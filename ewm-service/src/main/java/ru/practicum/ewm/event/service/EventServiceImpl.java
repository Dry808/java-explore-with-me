package ru.practicum.ewm.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.storage.CommentRepository;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.exceptions.ConflictException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.category.storage.CategoryRepository;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.user.storage.UserRepository;
import ru.practicum.ewm.event.storage.EventFilterBuilder;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.ewm.event.enums.EventState;

import ru.practicum.ewm.event.enums.SortOption;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Сервис для работы с Event
 */
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventFilterBuilder filterBuilder;
    private final StatsClient statsClient;
    private final CommentRepository commentRepository;

    // Получить событие по id (public)
    @Override
    @Transactional
    public EventFullDto getById(Long id, String remoteAddr) {
        Event publishedEvent = repository.findByIdAndState(id, EventState.PUBLISHED).orElseThrow(
                () -> new NotFoundException("мСобытие с id=%d не найдено".formatted(id))
        );

        EventFullDto dto = EventMapper.toDto(publishedEvent);

        dto.setViews(getViewsForEvents(List.of(publishedEvent)).get(dto.getId())); // установить кол-во просмотров

        return dto;
    }

    // получить событие по id(private)
    @Override
    @Transactional
    public EventFullDto getById(Long userId, Long eventId) {
        Event publishedEvent = repository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new NotFoundException("Событие с id=%d не найдено".formatted(eventId))
        );

        EventFullDto dto = EventMapper.toDto(publishedEvent);

        dto.setViews(getViewsForEvents(List.of(publishedEvent)).get(dto.getId()));

        return dto;
    }

    // Получить список событий с фильтром
    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAll(EventFilterDto filterDto) {
        BooleanExpression predicate = filterBuilder.buildPredicate(filterDto); // предикат

        Pageable pageable = PageRequest.of(// объект пагинации и сортировки
                filterDto.getFrom() / filterDto.getSize(),
                filterDto.getSize(),
                createSort(filterDto.getSort())
        );

        // Получить страницу событий с учетом фильтрации и пагинации
        Page<Event> page = repository.findAll(predicate, pageable);
        // количество просмотров для каждого события
        Map<Long, Long> eventsViews = getViewsForEvents(page.getContent());
        // Комментарии
        Map<Long, Long> eventsComments = getCommentsForEvents(page.getContent());


        return page.getContent()
                .stream()
                .map(EventMapper::toShortDto)
                .map(dto -> {
                            dto.setViews(eventsViews.get(dto.getId())); // установить просмотры
                            dto.setComments(eventsComments.get(dto.getId())); // установить комменты
                            return dto;
                        }
                )
                .toList();
    }

    // Получить список событий с фильтром (admin)
    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAll(AdminEventFilterDto filterDto) {
        BooleanExpression predicate = filterBuilder.buildPredicate(filterDto); // предикат

        Pageable pageable = PageRequest.of(// пагинация
                filterDto.getFrom() / filterDto.getSize(),
                filterDto.getSize()
        );

        Page<Event> page = repository.findAll(predicate, pageable);

        Map<Long, Long> eventsViews = getViewsForEvents(page.getContent());

        return page.getContent()
                .stream()
                .map(EventMapper::toDto)
                .map(dto -> {
                            dto.setViews(eventsViews.get(dto.getId()));
                            return dto;
                        }
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAll(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(// пагинация
                from / size,
                size
        );

        Page<Event> page = repository.findAllByInitiatorId(userId, pageable);

        Map<Long, Long> eventsViews = getViewsForEvents(page.getContent());

        return page.getContent()
                .stream()
                .map(EventMapper::toShortDto)
                .map(dto -> {
                            dto.setViews(eventsViews.get(dto.getId()));
                            return dto;
                        }
                )
                .toList();
    }

    // обновить
    @Override
    @Transactional
    public EventFullDto update(Long eventId, UpdateEventAdminRequest request) {
        Event oldEvent = repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие с id=%d не найдено".formatted(eventId))
        );

        // заголовок
        if (request.getTitle() != null && !request.getTitle().equals(oldEvent.getTitle())) {
            oldEvent.setTitle(request.getTitle());
        }

        // аннотация
        if (request.getAnnotation() != null && !request.getAnnotation().equals(oldEvent.getAnnotation())) {
            oldEvent.setAnnotation(request.getAnnotation());
        }

        // категория
        if (request.getCategory() != null) {
            Category newCategory = categoryRepository.findById(request.getCategory()).orElseThrow(
                    () -> new NotFoundException("Категория с id=%d не найдено".formatted(request.getCategory()))
            );

            if (!newCategory.equals(oldEvent.getCategory())) {
                oldEvent.setCategory(newCategory);
            }
        }
        // описание
        if (request.getDescription() != null && !request.getDescription().equals(oldEvent.getDescription())) {
            oldEvent.setDescription(request.getDescription());
        }
        // дата
        if (request.getEventDate() != null && !request.getEventDate().equals(oldEvent.getEventDate())) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Дата события должна быть не менее чем через час от текущего времени");
            }
            oldEvent.setEventDate(request.getEventDate());
        }

        // локация
        if (request.getLocation() != null && !request.getLocation().equals(oldEvent.getLocation())) {
            oldEvent.setLocation(request.getLocation());
        }

        // статус события
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case REJECT_EVENT -> {
                    if (oldEvent.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Опубликованные события не могут быть отклонены");
                    }
                    oldEvent.setState(EventState.CANCELED);
                }
                case PUBLISH_EVENT -> {
                    if (oldEvent.getState() != EventState.PENDING) {
                        throw new ConflictException(("Невозможно опубликовать событие, так как оно находится " +
                                "в неподходящем состоянии: %s").formatted(oldEvent.getState()));
                    }
                    oldEvent.setState(EventState.PUBLISHED);
                    oldEvent.setPublishedOn(LocalDateTime.now());
                }
            }
        }

        // статус оплаты
        if (request.getPaid() != null && !request.getPaid().equals(oldEvent.getPaid())) {
            oldEvent.setPaid(request.getPaid());
        }

        // лимит участников
        if (request.getParticipantLimit() != null
                && !request.getParticipantLimit().equals(oldEvent.getParticipantLimit())) {
            oldEvent.setParticipantLimit(request.getParticipantLimit());
        }

        // запросы
        if (request.getRequestModeration() != null
                && !request.getRequestModeration().equals(oldEvent.getRequestModeration())) {
            oldEvent.setRequestModeration(request.getRequestModeration());
        }

        return EventMapper.toDto(repository.save(oldEvent));
    }

    // обновление
    @Override
    @Transactional
    public EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event oldEvent = repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие с id=%d не найдено".formatted(eventId))
        );

        if (oldEvent.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Только ожидающие или отмененные события могут быть изменены");
        }

        if (request.getTitle() != null && !request.getTitle().equals(oldEvent.getTitle())) {
            oldEvent.setTitle(request.getTitle());
        }

        if (request.getAnnotation() != null && !request.getAnnotation().equals(oldEvent.getAnnotation())) {
            oldEvent.setAnnotation(request.getAnnotation());
        }

        if (request.getCategory() != null) {
            Category newCategory = categoryRepository.findById(request.getCategory()).orElseThrow(
                    () -> new NotFoundException("Категория с id=%d не найдена".formatted(request.getCategory()))
            );

            if (!newCategory.equals(oldEvent.getCategory())) {
                oldEvent.setCategory(newCategory);
            }
        }

        if (request.getDescription() != null && !request.getDescription().equals(oldEvent.getDescription())) {
            oldEvent.setDescription(request.getDescription());
        }

        if (request.getEventDate() != null && !request.getEventDate().equals(oldEvent.getEventDate())) {
            if (!request.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException(("Поле eventDate должно содержать дату, которая еще не наступила. " +
                        "Значение: %s").formatted(request.getEventDate().toString()));
            }

            oldEvent.setEventDate(request.getEventDate());
        }

        if (request.getLocation() != null && !request.getLocation().equals(oldEvent.getLocation())) {
            oldEvent.setLocation(request.getLocation());
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case CANCEL_REVIEW -> oldEvent.setState(EventState.CANCELED);
                case SEND_TO_REVIEW -> oldEvent.setState(EventState.PENDING);
            }
        }

        if (request.getPaid() != null && !request.getPaid().equals(oldEvent.getPaid())) {
            oldEvent.setPaid(request.getPaid());
        }

        if (request.getParticipantLimit() != null
                && !request.getParticipantLimit().equals(oldEvent.getParticipantLimit())) {
            oldEvent.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null
                && !request.getRequestModeration().equals(oldEvent.getRequestModeration())) {
            oldEvent.setRequestModeration(request.getRequestModeration());
        }

        return EventMapper.toDto(repository.save(oldEvent));
    }

    // создать
    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto event) {
        User initiator = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id=%d не найден".formatted(userId))
        );

        Category category = categoryRepository.findById(event.getCategory()).orElseThrow(
                () -> new NotFoundException("Категория с id=%d не найдена".formatted(event.getCategory()))
        );

        if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException(("Поле eventDate должно содержать дату, которая еще не наступила. " +
                    "Значение: %s").formatted(event.getEventDate().toString()));
        }
        // создаём новое событие
        return EventMapper.toDto(repository.save(Event.builder()
                .title(event.getTitle())
                .description(event.getDescription())
                .annotation(event.getAnnotation())
                .category(category)
                .initiator(initiator)
                .state(EventState.PENDING)
                .eventDate(event.getEventDate())
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .build()));
    }

    // для сортировки
    private Sort createSort(SortOption sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "id"); // сортировка по id
        }

        return switch (sort) {
            case VIEWS -> Sort.by(Sort.Direction.DESC, "views"); // по кол-во просмотров
            case EVENT_DATE -> Sort.by(Sort.Direction.DESC, "eventDate"); // сортировка по дате
        };
    }

    // получает кол-во просмотров для списка событий
    private Map<Long, Long> getViewsForEvents(List<Event> events) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // найти самую ранюю дату создания event
        LocalDateTime earliestEvent = events.getFirst().getCreatedOn();

        for (Event event : events) {
            if (event.getCreatedOn().isBefore(earliestEvent)) {
                earliestEvent = event.getCreatedOn();
            }
        }
        // дата начала и конца
        String start = earliestEvent.format(formatter);
        String end = LocalDateTime.now().format(formatter);

        // мапа для сопаставления uri и id Event
        Map<String, Long> uriIdMap = new HashMap<>();

        // создаёт список uri для запроса статистикаи
        List<String> eventsUris = events.stream()
                .map(event -> {
                    String uri = "/events/" + event.getId();

                    uriIdMap.put(uri, event.getId());

                    return uri;
                }).toList();

        ResponseEntity<Object> response = statsClient.getStats(start, end, eventsUris, true);

        Object responseBody = response.getBody();

        ObjectMapper mapper = new ObjectMapper();

        List<ViewStatsDto> viewStatsDtoList = mapper.convertValue(responseBody,
                new TypeReference<>() {
                });

        Map<Long, Long> idsToViewsMap = new HashMap<>();

        if (viewStatsDtoList.isEmpty()) {
            for (Event event : events) {
                idsToViewsMap.put(event.getId(), 0L); //Если статистика отсутствует, устанавливаем 0 просмотров
            }
        } else {
            for (ViewStatsDto viewStatsDto : viewStatsDtoList) {
                idsToViewsMap.put(uriIdMap.get(viewStatsDto.getUri()), viewStatsDto.getHits());
            }
        }

        return idsToViewsMap;
    }

    private Map<Long, Long> getCommentsForEvents(List<Event> events) {

        List<Comment> comments = commentRepository.findByEventIdInAndIsModeratedTrue(events.stream().map(Event::getId).toList());

        Map<Long, Long> idsToCommentsMap = new HashMap<>();

        for (Event event : events) {
            idsToCommentsMap.put(event.getId(), comments
                    .stream().filter(comment -> Objects.equals(comment.getEvent()
                            .getId(), event.getId())).count());
        }

        return idsToCommentsMap;
    }
}
