package ru.practicum.ewm.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.StatDto;
import ru.practicum.StatOutDto;
import ru.practicum.client.StatisticsClient;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.comment.CommentRepository;
import ru.practicum.ewm.comment.CountCommentsByEventDto;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.location.LocationMapper;
import ru.practicum.ewm.event.model.*;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidatetionConflict;
import ru.practicum.ewm.location.Location;
import ru.practicum.ewm.location.LocationRepository;
import ru.practicum.ewm.request.*;
import ru.practicum.ewm.request.service.ParticipationRequestDto;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication(scanBasePackages = {"ru.practicum.client"})  // для Idea
@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceBase implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final LocationRepository locationRepository;
    private final CommentRepository commentRepository;
    private final StatisticsClient statsClient;
    private final ObjectMapper objectMapper;

    @Value("${server.application.name:ewm-service}")
    private String applicationName;

    @Override
    public List<EventFullDto> getAllAdmin(EventAdminParams eventParamsAdmin) {
        PageRequest pageable = PageRequest.of(eventParamsAdmin.getFrom() / eventParamsAdmin.getSize(),
                eventParamsAdmin.getSize());

        Specification<Event> specification = Specification.where(null);

        List<Long> users = eventParamsAdmin.getUsers();
        List<String> states = eventParamsAdmin.getStates();
        List<Long> categories = eventParamsAdmin.getCategories();
        LocalDateTime rangeEnd = eventParamsAdmin.getRangeEnd();
        LocalDateTime rangeStart = eventParamsAdmin.getRangeStart();

        if (users != null && !users.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("initiator").get("id").in(users));
        }

        if (states != null && !states.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("eventStatus").as(String.class).in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(categories));
        }

        if (rangeEnd != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }

        if (rangeStart != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }

        Page<Event> events = eventRepository.findAll(specification, pageable);

        List<EventFullDto> result = events.getContent().stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());

        Map<Long, List<Request>> confirmedRequestsCountMap = getConfirmedRequestsCount(events.toList());

        for (EventFullDto event : result) {
            List<Request> requests = confirmedRequestsCountMap.getOrDefault(event.getId(), List.of());
            event.setConfirmedRequests(requests.size());
        }

        return result;
    }

    @Override
    public EventFullDto updateAdmin(Long eventId, UpdateEventAdminRequest updateEvent) {
        Event oldEvent = checkEvent(eventId);

        if (oldEvent.getEventStatus().equals(EventStatus.PUBLISHED) || oldEvent.getEventStatus().equals(EventStatus.CANCELED)) {
            throw new ValidatetionConflict("Событие со статусом status= " + oldEvent.getEventStatus() +  "изменить нельзя");
        }

        Event eventForUpdate = eventUpdateBase(oldEvent, updateEvent);

        if (updateEvent.getEventDate() != null) {
            if (updateEvent.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Некорректные параметры даты. Дата начала события не может быть ранее часа от момента публикации.");
            }
            eventForUpdate.setEventDate(updateEvent.getEventDate());
        }

        if (updateEvent.getStateAction() != null) {
            if (EventAdminState.PUBLISH_EVENT.equals(updateEvent.getStateAction())) {
                eventForUpdate.setEventStatus(EventStatus.PUBLISHED);

            } else if (EventAdminState.REJECT_EVENT.equals(updateEvent.getStateAction())) {
                eventForUpdate.setEventStatus(EventStatus.CANCELED);
            }
        }

        eventRepository.save(eventForUpdate);

        return EventMapper.toEventFullDto(eventForUpdate);
    }

    @Override
    public List<EventShortDto> getByUserId(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id= " + userId + " не найден");
        }
        PageRequest pageRequest = PageRequest.of(from / size, size, org.springframework.data.domain.Sort.by(Sort.Direction.ASC, "id"));
        return eventRepository.findAll(pageRequest).getContent()
                .stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto getById(Long eventId, HttpServletRequest request) {
        Event event = checkEvent(eventId);

        if (!event.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new NotFoundException("Событие с id = " + eventId + " не опубликовано");
        }

        statsClient.postHit(StatDto.builder()
                .app(applicationName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());

        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        Map<Long, Long> viewStatsMap = getViews(List.of(event));
        Long views = viewStatsMap.getOrDefault(event.getId(), 1L);
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        LocalDateTime createdOn = LocalDateTime.now();
        User user = checkUser(userId);
        checkDateAndTime(LocalDateTime.now(), newEventDto.getEventDate());
        Category category = checkCategory(newEventDto.getCategory());
        Event event = EventMapper.toEvent(newEventDto);
        event.setCategory(category);
        event.setInitiator(user);
        event.setEventStatus(EventStatus.PENDING);
        event.setCreatedDate(createdOn);

        if (newEventDto.getLocation() != null) {
            Location location = locationRepository.save(LocationMapper.toLocation(newEventDto.getLocation()));
            event.setLocation(location);
        }

        Event eventSaved = eventRepository.save(event);

        EventFullDto eventFullDto = EventMapper.toEventFullDto(eventSaved);
        eventFullDto.setViews(0L);
        eventFullDto.setConfirmedRequests(0);
        return eventFullDto;
    }

    @Override
    public EventFullDto updateByUserIdAndEventId(Long userId, Long eventId, UpdateEventUserRequest eventUpdate) {
        checkUser(userId);
        Event oldEvent = checkEvenByInitiatorAndEventId(userId, eventId);

        if (oldEvent.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new ValidatetionConflict("Статус события в статусе status= " + oldEvent.getEventStatus() + "не может быть обновлен");
        }

        if (!oldEvent.getInitiator().getId().equals(userId)) {
            throw new ValidatetionConflict("Пользователь с id= " + userId + " не является автором события");
        }

        Event eventForUpdate = eventUpdateBase(oldEvent, eventUpdate);

        LocalDateTime newDate = eventUpdate.getEventDate();

        if (newDate != null) {
            checkDateAndTime(LocalDateTime.now(), newDate);
            eventForUpdate.setEventDate(newDate);
        }
        EventUserState stateAction = eventUpdate.getStateAction();

        if (stateAction != null) {
            switch (stateAction) {
                case SEND_TO_REVIEW:
                    eventForUpdate.setEventStatus(EventStatus.PENDING);
                    break;
                case CANCEL_REVIEW:
                    eventForUpdate.setEventStatus(EventStatus.CANCELED);
                    break;
            }
        }
        if (eventForUpdate != null) {
            eventRepository.save(eventForUpdate);
        }

        return eventForUpdate != null ? EventMapper.toEventFullDto(eventForUpdate) : null;
    }

    @Override
    public EventFullDto getByUserIdAndEventId(Long userId, Long eventId) {
        checkUser(userId);
        Event event = checkEvenByInitiatorAndEventId(userId, eventId);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> getAllParticipationRequestsByOwner(Long userId, Long eventId) {
        checkUser(userId);
        checkEvenByInitiatorAndEventId(userId, eventId);
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream().map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());

    }

    @Override
    public List<EventShortDto> getAllPublic(EventParams eventParams, HttpServletRequest request) {

        if (eventParams.getRangeEnd() != null && eventParams.getRangeStart() != null) {
            if (eventParams.getRangeEnd().isBefore(eventParams.getRangeStart())) {
                throw new ValidationException("Дата окончания не может быть раньше даты начала");
            }
        }

        statsClient.postHit(StatDto.builder()
                .app(applicationName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());

        Pageable pageable = PageRequest.of(eventParams.getFrom() / eventParams.getSize(), eventParams.getSize());

        Specification<Event> specification = Specification.where(null);
        LocalDateTime now = LocalDateTime.now();

        if (eventParams.getText() != null) {
            String searchText = eventParams.getText().toLowerCase();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + searchText + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + searchText + "%")
                    ));
        }

        if (eventParams.getCategories() != null && !eventParams.getCategories().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(eventParams.getCategories()));
        }

        LocalDateTime startDateTime = Objects.requireNonNullElse(eventParams.getRangeStart(), now);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventDate"), startDateTime));

        if (eventParams.getRangeEnd() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("eventDate"), eventParams.getRangeEnd()));
        }

        if (eventParams.getOnlyAvailable() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("participantLimit"), 0));
        }

        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("eventStatus"), EventStatus.PUBLISHED));

        List<Event> resultEvents = eventRepository.findAll(specification, pageable).getContent();
        List<EventShortDto> result = resultEvents
                .stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
        Map<Long, Long> viewStatsMap = getViews(resultEvents);

        List<CountCommentsByEventDto> commentsCountMap = commentRepository.countCommentByEvent(
                resultEvents.stream().map(Event::getId).collect(Collectors.toList()));
        Map<Long, Long> commentsCountToEventIdMap = commentsCountMap.stream().collect(Collectors.toMap(
                CountCommentsByEventDto::getEventId, CountCommentsByEventDto::getCountComments));

        for (EventShortDto event : result) {
            Long viewsFromMap = viewStatsMap.getOrDefault(event.getId(), 0L);
            event.setViews(viewsFromMap);

            Long commentCountFromMap = commentsCountToEventIdMap.getOrDefault(event.getId(), 0L);
            event.setComments(commentCountFromMap);
        }

        return result;
    }

    private List<Request> checkRequestOrEventList(Long eventId, List<Long> requestId) {
        return requestRepository.findByEventIdAndIdIn(eventId, requestId).orElseThrow(
                () -> new NotFoundException("Запроса с id = " + requestId + " или события с id = "
                        + eventId + "не существуют"));
    }

    private Event checkEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = " + eventId + " не существует"));
    }

    private Category checkCategory(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категории с id = " + catId + " не существует"));
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователя с id = " + userId + " не существует"));
    }

    private Location checkLocation(Long locationId) {
        return locationRepository.findById(locationId).orElseThrow(
                () -> new NotFoundException("Локайшена с id = " + locationId + " не существует"));
    }

    private void checkDateAndTime(LocalDateTime time, LocalDateTime dateTime) {
        if (dateTime.isBefore(time.plusHours(2))) {
            throw new ValidationException("Поле должно содержать дату, которая еще не наступила.");
        }
    }

    private Event checkEvenByInitiatorAndEventId(Long userId, Long eventId) {
        return eventRepository.findByInitiatorIdAndId(userId, eventId).orElseThrow(
                () -> new NotFoundException("События с id = " + eventId + "и с пользователем с id = " + userId +
                        " не существует"));
    }

    private Map<Long, Long> getViews(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());

        List<LocalDateTime> startDates = events.stream()
                .map(Event::getCreatedDate)
                .collect(Collectors.toList());
        LocalDateTime earliestDate = startDates.stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);
        Map<Long, Long> viewStatsMap = new HashMap<>();

        if (earliestDate != null) {
            ResponseEntity<Object> response = statsClient.getStatistics(earliestDate.toString(), LocalDateTime.now().toString(),uris, true); ///????
            List<StatOutDto> statOutDtoList = objectMapper.convertValue(response.getBody(), new TypeReference<>() {}); ///// ????

            viewStatsMap = statOutDtoList.stream()
                    .filter(statsDto -> statsDto.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(statsDto -> Long.parseLong(statsDto.getUri().substring("/events/".length())),StatOutDto::getHits));
        }
        return viewStatsMap;
    }

    private Map<Long, List<Request>> getConfirmedRequestsCount(List<Event> events) {

        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(events
                .stream().map(Event::getId).collect(Collectors.toList()), RequestStatus.CONFIRMED);
        return requests.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId()));
    }

    private List<Request> rejectRequest(List<Long> ids, Long eventId) {
        List<Request> rejectedRequests = new ArrayList<>();
        List<Request> requestList = new ArrayList<>();
        List<Request> requestListLoaded = checkRequestOrEventList(eventId, ids);

        for (Request request : requestListLoaded) {

            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                break;
            }

            request.setStatus(RequestStatus.REJECTED);
            requestList.add(request);
            rejectedRequests.add(request);
        }
        requestRepository.saveAll(requestList);
        return rejectedRequests;
    }

    private Event eventUpdateBase(Event event, UpdateEventBase updateEvent) {

        if (updateEvent.getAnnotation() != null && !updateEvent.getAnnotation().isBlank()) {
            event.setAnnotation(updateEvent.getAnnotation());
        }

        Long gotCategory = updateEvent.getCategory();

        if (gotCategory != null) {
            Category category = checkCategory(gotCategory);
            event.setCategory(category);
        }

        if (updateEvent.getDescription() != null && !updateEvent.getDescription().isBlank()) {
            event.setDescription(updateEvent.getDescription());
        }

        if (updateEvent.getLocation() != null) {
            event.setLocation(LocationMapper.toLocation(updateEvent.getLocation()));
        }

        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }

        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }

        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }

        if (updateEvent.getTitle() != null && !updateEvent.getTitle().isBlank()) {
            event.setTitle(updateEvent.getTitle());
        }

        return event;
    }

    @Override
    public Map<String, List<ParticipationRequestDto>> approveRequests(final Long userId, final Long eventId,
                                                                      final EventRequestStatusUpdateRequest requestUpdateDto) {

        final User user = checkUser(userId);

        final Event event = checkEvent(eventId);

        if (!Objects.equals(event.getInitiator(), user)) {
            throw new ValidatetionConflict("Пользователь не является инициатором этого события.");
        }

        final List<Request> requests = requestRepository.findRequestByIdIn(requestUpdateDto.getRequestIds().stream().toList());

        if (event.isRequestModeration() && event.getParticipantLimit().equals(event.getConfirmedRequests()) &&
                event.getParticipantLimit() != 0 && requestUpdateDto.getStatus().equals(RequestStatus.CONFIRMED)) {
            throw new ValidatetionConflict("Лимит заявок на участие в событии исчерпан.");
        }

        final boolean verified = requests.stream()
                .allMatch(request -> request.getEvent().getId().longValue() == eventId);
        if (!verified) {
            throw new ValidatetionConflict("Список запросов не относятся к одному событию.");
        }

        final Map<String, List<ParticipationRequestDto>> requestMap = new HashMap<>();

        if (requestUpdateDto.getStatus().equals(RequestStatus.REJECTED)) {
            if (requests.stream()
                    .anyMatch(request -> request.getStatus().equals(RequestStatus.CONFIRMED))) {
                throw new ValidatetionConflict("Запрос на установление статуса <ОТМЕНЕНА>. Подтвержденые заявки нельзя отменить.");
            }
            log.info("Запрос на отклонение заявки подтвержден.");

            List<ParticipationRequestDto> rejectedRequests = requests.stream()
                    .peek(request -> request.setStatus(RequestStatus.REJECTED))
                    .map(requestRepository::save)
                    .map(RequestMapper::toParticipationRequestDto)
                    .toList();
            requestMap.put("rejectedRequests", rejectedRequests);
        } else {
            if (requests.stream()
                    .anyMatch(request -> !request.getStatus().equals(RequestStatus.PENDING))) {
                throw new ValidatetionConflict("Запрос на установление статуса <ПОДТВЕРЖДЕНА>. Заявки должны быть со статусом <В ОЖИДАНИИ>.");
            }
            Integer confRequests = 0;

            if (event.getConfirmedRequests() != null) {
                confRequests = event.getConfirmedRequests();
            }

            long limit = event.getParticipantLimit() - confRequests;
            final List<Request> confirmedList = requests.stream()
                    .limit(limit)
                    .peek(request -> request.setStatus(RequestStatus.CONFIRMED))
                    .map(requestRepository::save).toList();
            log.info("Заявки на участие сохранены со статусом <ПОДТВЕРЖДЕНА>.");

            final List<ParticipationRequestDto> confirmedRequests = confirmedList.stream()
                    .map(RequestMapper::toParticipationRequestDto)
                    .toList();
            requestMap.put("confirmedRequests", confirmedRequests);

            final List<Request> rejectedList = requests.stream()
                    .skip(limit)
                    .peek(request -> request.setStatus(RequestStatus.REJECTED))
                    .map(requestRepository::save).toList();
            log.info("Часть заявок на участие сохранены со статусом <ОТМЕНЕНА>, в связи с превышением лимита.");
            final List<ParticipationRequestDto> rejectedRequests = rejectedList.stream()
                    .map(RequestMapper::toParticipationRequestDto)
                    .toList();
            requestMap.put("rejectedRequests", rejectedRequests);

            if (event.getConfirmedRequests() != null) {
                confRequests = event.getConfirmedRequests();
            }
            event.setConfirmedRequests(confirmedList.size() + confRequests);
            eventRepository.save(event);
        }
        return requestMap;
    }

}