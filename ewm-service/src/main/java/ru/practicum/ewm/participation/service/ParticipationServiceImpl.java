package ru.practicum.ewm.participation.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.exceptions.ConflictException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.participation.model.Participation;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.event.enums.EventState;
import ru.practicum.ewm.event.enums.RequestStatus;
import ru.practicum.ewm.participation.mapper.ParticipationMapper;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.participation.storage.ParticipationRepository;
import ru.practicum.ewm.user.storage.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сервис для работы с участниками
 */
@Service
@RequiredArgsConstructor
public class ParticipationServiceImpl implements ParticipationService {
    private final ParticipationRepository repository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;

    // Получить список запросов события
    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        return repository.findByEvent_Id(eventId).stream()
                .filter(participation -> Objects.equals(participation.getEvent().getInitiator().getId(), userId))
                .map(ParticipationMapper::toDto)
                .toList();
    }

    // Обновить статус запроса
    @Override
    @Transactional
    public EventRequestStatusUpdateResult setRequestsStatusResults(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new NotFoundException("Событие с  id=%d не найдено".formatted(eventId))
        );

        // проверка потверждения
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Подтверждение для этого события не требуется");
        }

        // проверка лимита
        if (Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())) {
            throw new ConflictException("Лимит участников достигнут");
        }

        // запросы по id
        List<Participation> requests = repository.findByIdInAndEventId(updateRequest.getRequestIds(), eventId);

        // проверка статусов запросов = pending
        for (Participation request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Только статус Pending может быть изменен");
            }
        }

        int confirmedCount = event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0;
        int participantLimit = event.getParticipantLimit();

        List<Participation> confirmedRequests = new ArrayList<>();
        List<Participation> rejectedRequests = new ArrayList<>();
        QEvent qEvent = QEvent.event;

        if (updateRequest.getStatus() == RequestStatus.CONFIRMED) { // обработка
            for (Participation request : requests) {
                if (confirmedCount >= participantLimit) {
                    throw new ConflictException("Лимит участников достигнут");
                }

                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);

                // Обновить количество подтверждённых запросов
                queryFactory.update(qEvent)
                        .set(qEvent.confirmedRequests, qEvent.confirmedRequests.add(1))
                        .where(qEvent.id.eq(request.getEvent().getId()))
                        .execute();

                confirmedCount++;

                if (confirmedCount == participantLimit) { // Если лимит участников достигнут, отклонить запросы
                    List<Participation> pendingRequests = repository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);

                    for (Participation pendingRequest : pendingRequests) {
                        pendingRequest.setStatus(RequestStatus.REJECTED);
                        rejectedRequests.add(pendingRequest);
                    }

                    repository.saveAll(rejectedRequests);

                    break;
                }
            }

            repository.saveAll(confirmedRequests);

            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(confirmedRequests.stream().map(ParticipationMapper::toDto).toList())
                    .rejectedRequests(rejectedRequests.stream().map(ParticipationMapper::toDto).toList())
                    .build();
        } else if (updateRequest.getStatus() == RequestStatus.REJECTED) {
            for (Participation request : requests) { // обработка отклонений
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }

            repository.saveAll(rejectedRequests);

            return EventRequestStatusUpdateResult.builder()
                    .rejectedRequests(rejectedRequests.stream().map(ParticipationMapper::toDto).toList())
                    .build();
        } else {
            throw new ConflictException("Неправильный статус");
        }
    }

    // запросы пользователя
    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User c id=%d не найден".formatted(userId))
        );
        return repository.findByRequesterId(userId).stream()
                .map(ParticipationMapper::toDto)
                .toList();
    }

    // создать новый запрос
    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event c id=%d не найден".formatted(eventId))
        );

        User requester = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User c id=%d не найден".formatted(userId))
        );

        // проверка дубликатов
        List<Participation> participations = repository.findByEvent_IdAndRequester_Id(eventId, userId);
        if (!participations.isEmpty()) {
            throw new ConflictException("Дублирующий запрос на событие");
        }

        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("Инициатор не может быть участником своего мероприятия.");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Это событие еще не опубликовано");
        }

        if (event.getParticipantLimit() != 0) {
            if (Objects.equals(event.getParticipantLimit(), event.getConfirmedRequests())) {
                throw new ConflictException("Достигнут лимит участников");
            }
        }

        // Если подтверждение не требуется, автоматически подтвердить запрос
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            QEvent qEvent = QEvent.event;

            queryFactory.update(qEvent)
                    .set(qEvent.confirmedRequests, qEvent.confirmedRequests.add(1))
                    .where(qEvent.id.eq(event.getId()))
                    .execute();

            return ParticipationMapper.toDto(repository.save(Participation.builder()
                    .event(event)
                    .requester(requester)
                    .status(RequestStatus.CONFIRMED)
                    .build()));
        }
        // Создать запрос на участие со статусом PENDING
        return ParticipationMapper.toDto(repository.save(Participation.builder()
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .build()));
    }

    // отмена запроса
    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Participation request = repository.findByIdAndRequesterId(requestId, userId).orElseThrow(
                () -> new NotFoundException("Запрос с id=%d не найден".formatted(requestId))
        );

        request.setStatus(RequestStatus.CANCELED); // статус

        return ParticipationMapper.toDto(repository.save(request));
    }
}
