package ru.practicum.event.service;


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
import ru.practicum.category.model.Category;
import ru.practicum.category.storage.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import ru.practicum.event.model.SortOption;
import ru.practicum.event.storage.EventFilterBuilder;
import ru.practicum.event.storage.EventRepository;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;

import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventFilterBuilder filterBuilder;
    private final StatsClient statsClient;

    @Override
    public EventFullDto getById(Long id, String remoteAddr) {
        //поиск события по ID и статусу опубликован
        Event publishedEvent = repository.findByIdAndState(id, EventState.PUBLISHED).orElseThrow(
                () -> new NotFoundException("Event с id=%d не найден".formatted(id))
        );

        EventFullDto dto = EventMapper.toDto(publishedEvent);
        dto.setViews(getViewsForEvents(List.of(publishedEvent)).get(dto.getId()));

        return dto;
    }

    @Override
    public EventFullDto getById(Long userId, Long eventId) {
        Event publishedEvent = repository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new NotFoundException("Event с id=%d не найден".formatted(eventId))
        );

        EventFullDto dto = EventMapper.toDto(publishedEvent);

        dto.setViews(getViewsForEvents(List.of(publishedEvent)).get(dto.getId()));

        return dto;
    }


    @Override
    public List<EventShortDto> getAll(EventFilterDto filterDto) {
        // Создание предиката для фильтрации событий
        BooleanExpression predicate = filterBuilder.buildPredicate(filterDto);

        // Создание пагинации и сортировки
        Pageable pageable = PageRequest.of(
                filterDto.getFrom() / filterDto.getSize(), // Вычисление номера страницы
                filterDto.getSize(), // Размер страницы
                createSort(filterDto.getSort()) // Создание сортировки
        );

        // Получение страницы событий с учетом фильтрации, пагинации и сортировки
        Page<Event> page = repository.findAll(predicate, pageable);

        // Получение количества просмотров для каждого события
        Map<Long, Long> eventsViews = getViewsForEvents(page.getContent());

        // Преобразование событий в DTO и установка количества просмотров
        return page.getContent()
                .stream()
                .map(EventMapper::toShortDto) // Преобразование Event в EventShortDto
                .map(dto -> {
                            dto.setViews(eventsViews.get(dto.getId())); // Установка количества просмотров
                            return dto;
                        }
                )
                .toList(); // Преобразование в список
    }

    @Override
    public List<EventFullDto> getAll(AdminEventFilterDto filterDto) {
        BooleanExpression predicate = filterBuilder.buildPredicate(filterDto);

        Pageable pageable = PageRequest.of(
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
    public List<EventShortDto> getAll(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(
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

    @Override
    public EventFullDto update(Long eventId, UpdateEventAdminRequest request) {
        Event oldEvent = repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event с id=%d не найден".formatted(eventId))
        );

        if (request.getTitle() != null && !request.getTitle().equals(oldEvent.getTitle())) {
            oldEvent.setTitle(request.getTitle());
        }

        if (request.getAnnotation() != null && !request.getAnnotation().equals(oldEvent.getAnnotation())) {
            oldEvent.setAnnotation(request.getAnnotation());
        }

        if (request.getCategory() != null) {
            Category newCategory = categoryRepository.findById(request.getCategory()).orElseThrow(
                    () -> new NotFoundException("Category с id=%d не найден".formatted(request.getCategory()))
            );

            if (!newCategory.equals(oldEvent.getCategory())) {
                oldEvent.setCategory(newCategory);
            }
        }

        if (request.getDescription() != null && !request.getDescription().equals(oldEvent.getDescription())) {
            oldEvent.setDescription(request.getDescription());
        }

        if (request.getEventDate() != null && !request.getEventDate().equals(oldEvent.getEventDate())) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Дата события должна быть позже текущего времени");
            }
            oldEvent.setEventDate(request.getEventDate());
        }

        if (request.getLocation() != null && !request.getLocation().equals(oldEvent.getLocation())) {
            oldEvent.setLocation(request.getLocation());
        }

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
                        throw new ConflictException("неправильное состояние: %s".formatted(oldEvent.getState()));
                    }
                    oldEvent.setState(EventState.PUBLISHED);
                    oldEvent.setPublishedOn(LocalDateTime.now());
                }
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

    @Override
    public EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event oldEvent = repository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event с id=%d не найдено".formatted(eventId))
        );

        if (oldEvent.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Т");
        }

        if (request.getTitle() != null && !request.getTitle().equals(oldEvent.getTitle())) {
            oldEvent.setTitle(request.getTitle());
        }

        if (request.getAnnotation() != null && !request.getAnnotation().equals(oldEvent.getAnnotation())) {
            oldEvent.setAnnotation(request.getAnnotation());
        }

        if (request.getCategory() != null) {
            Category newCategory = categoryRepository.findById(request.getCategory()).orElseThrow(
                    () -> new NotFoundException("Category with id=%d was not found".formatted(request.getCategory()))
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
                throw new ConflictException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s".formatted(request.getEventDate().toString()));
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

    @Override
    public EventFullDto create(Long userId, NewEventDto event) {
        User initiator = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id=%d was not found".formatted(userId))
        );

        Category category = categoryRepository.findById(event.getCategory()).orElseThrow(
                () -> new NotFoundException("Category with id=%d was not found".formatted(event.getCategory()))
        );

        if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s".formatted(event.getEventDate().toString()));
        }

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

    private Sort createSort(SortOption sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "id");
        }

        return switch (sort) {
            case VIEWS -> Sort.by(Sort.Direction.DESC, "views");
            case EVENT_DATE -> Sort.by(Sort.Direction.DESC, "eventDate");
        };
    }

    // метод для получения количества просмотров для списка событий
    private Map<Long, Long> getViewsForEvents(List<Event> events) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime earliestEvent = events.getFirst().getCreatedOn();

        for (Event event : events) {
            if (event.getCreatedOn().isBefore(earliestEvent)) {
                earliestEvent = event.getCreatedOn();
            }
        }

        String start = earliestEvent.format(formatter);

        String end = LocalDateTime.now().format(formatter);

        Map<String, Long> uriIdMap = new HashMap<>();

        List<String> eventsUris = events.stream()
                .map(event -> {
                    String uri = "/events/" + event.getId();

                    uriIdMap.put(uri, event.getId());

                    return uri;
                }).toList();

        ResponseEntity<Object> response = statsClient.getStats(start, end, eventsUris, true);

        Object responseBody = response.getBody();

        ObjectMapper mapper = new ObjectMapper();

        List<ViewStatsDto> viewStatsList = mapper.convertValue(responseBody,
                new TypeReference<>() {
                });

        Map<Long, Long> idsToViewsMap = new HashMap<>();

        if (viewStatsList.isEmpty()) {
            for (Event event : events) {
                idsToViewsMap.put(event.getId(), 0L);
            }
        } else {
            for (ViewStatsDto viewStats : viewStatsList) {
                idsToViewsMap.put(uriIdMap.get(viewStats.getUri()), viewStats.getHits());
            }
        }

        return idsToViewsMap;
    }
}
