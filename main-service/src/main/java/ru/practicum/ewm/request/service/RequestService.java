package ru.practicum.ewm.request.service;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto create(Long userId, Long eventId);

    List<ParticipationRequestDto> getByUserId(Long userId);

    ParticipationRequestDto cancel(Long userId, Long requestId);
}