package ru.practicum.participation.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.QEvent;
import ru.practicum.event.model.RequestStatus;
import ru.practicum.event.storage.EventRepository;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.participation.dto.ParticipationRequestDto;
import ru.practicum.participation.mapper.ParticipationMapper;
import ru.practicum.participation.model.Participation;
import ru.practicum.participation.storage.ParticipationRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сервис для логики обработки запросов на участие в Event
 */
@Service
@RequiredArgsConstructor
public class ParticipationServiceImpl implements ParticipationService {
    private final ParticipationRepository repository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;

    //Получение списка запросов на участие в всобытии
    @Override
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        return repository.findByEvent_Id(eventId).stream()
                .filter(participation -> Objects.equals(participation.getEvent().getInitiator().getId(), userId))
                .map(ParticipationMapper::toDto)
                .toList();
    }

    // Обновлять статус запросов на участие в event
    @Override
    public EventRequestStatusUpdateResult setRequestsStatusResults(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        //проверка
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new NotFoundException("Событие с id=%d не найдено".formatted(eventId))
        );
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Подтверждение не требуется");
        }
        if (Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())) {
            throw new ConflictException("достигнут лимит участников");
        }

        // Получаем запросы на участие по их ID и ID события
        List<Participation> requests = repository.findByIdInAndEventId(updateRequest.getRequestIds(), eventId);
        // Проверка статуса
        for (Participation request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Изменить можно только запросов со статусом PENDING");
            }
        }
        int confirmedCount = event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0;
        int participantLimit = event.getParticipantLimit();

        List<Participation> confirmedRequests = new ArrayList<>();
        List<Participation> rejectedRequests = new ArrayList<>();
        QEvent qEvent = QEvent.event;

        // Если статус обновления - CONFIRMED
        if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
            for (Participation request : requests) {
                if (confirmedCount >= participantLimit) {
                    throw new ConflictException("Лимит участников достигнут");
                }

                // Обновляем статус запроса на CONFIRMED
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);

                // Увеличиваем количество подтвержденных запросов в событии
                queryFactory.update(qEvent)
                        .set(qEvent.confirmedRequests, qEvent.confirmedRequests.add(1))
                        .where(qEvent.id.eq(request.getEvent().getId()))
                        .execute();

                confirmedCount++;

                // Если лимит участников достигнут отклоняем оставшиеся запросы
                if (confirmedCount == participantLimit) {
                    List<Participation> pendingRequests = repository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);

                    for (Participation pendingRequest : pendingRequests) {
                        pendingRequest.setStatus(RequestStatus.REJECTED);
                        rejectedRequests.add(pendingRequest);
                    }

                    repository.saveAll(rejectedRequests);

                    break;
                }
            }

            // cохраняем подтвержденные запросы
            repository.saveAll(confirmedRequests);

            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(confirmedRequests.stream().map(ParticipationMapper::toDto).toList())
                    .rejectedRequests(rejectedRequests.stream().map(ParticipationMapper::toDto).toList())
                    .build();
        } else if (updateRequest.getStatus() == RequestStatus.REJECTED) {
            for (Participation request : requests) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }

            repository.saveAll(rejectedRequests);

            return EventRequestStatusUpdateResult.builder()
                    .rejectedRequests(rejectedRequests.stream().map(ParticipationMapper::toDto).toList())
                    .build();
        } else {
            throw new ConflictException("Неправильный статус обновления");
        }
    }

    // список запросов на участие для пользователя
    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User c id=%d не найден".formatted(userId))
        );
        // запросы на участие для пользователя
        return repository.findByRequesterId(userId).stream()
                .map(ParticipationMapper::toDto)
                .toList();
    }

    // Создает запрос на участие в событии
    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        //проверки
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event с id=%d не найден".formatted(eventId))
        );
        User requester = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User с id=%d не найден".formatted(userId))
        );

        /// Проверяем, не существует ли уже запроса от этого пользователя на это событие
        List<Participation> participations = repository.findByEvent_IdAndRequester_Id(eventId, userId);
        if (!participations.isEmpty()) {
            throw new ConflictException("Запрос на дублирование события");
        }

        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("Инициатор не может быть участником своего event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Это событие еще не опубликовано");
        }

        if (event.getParticipantLimit() != 0) {
            if (Objects.equals(event.getParticipantLimit(), event.getConfirmedRequests())) {
                throw new ConflictException("Достигнут лимит участников");
            }
        }

        // Если подтверждение не требуется подтверждаем запрос
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            QEvent qEvent = QEvent.event;

            // Увеличиваем количество подтвержденных запросов
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
        return ParticipationMapper.toDto(repository.save(Participation.builder()
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .build()));
    }

    // Отмена запроса на участие в событии
    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Participation request = repository.findByIdAndRequesterId(requestId, userId).orElseThrow(
                () -> new NotFoundException("Request with id=%d was not found".formatted(requestId))
        );

        // Обновляем статус запроса на CANCELED
        request.setStatus(RequestStatus.CANCELED);

        // Сохраняем и возвращаем обновленный запрос
        return ParticipationMapper.toDto(repository.save(request));
    }
}
