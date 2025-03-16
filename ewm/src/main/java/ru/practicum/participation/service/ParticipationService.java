package ru.practicum.participation.service;

import ru.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.participation.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationService {
    List<ParticipationRequestDto> getRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult setRequestsStatusResults(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<ParticipationRequestDto> getRequestsByUserId(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}
