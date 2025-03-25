package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.*;


import java.util.List;

public interface EventService {
    EventFullDto getById(Long id, String remoteAddr);

    EventFullDto getById(Long userId, Long eventId);

    List<EventShortDto> getAll(EventFilterDto filterDto);

    List<EventFullDto> getAll(AdminEventFilterDto filterDto);

    List<EventShortDto> getAll(Long userId, Integer from, Integer size);

    EventFullDto update(Long eventId, UpdateEventAdminRequest request);

    EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest request);

    EventFullDto create(Long userId, NewEventDto eventDto);
}
