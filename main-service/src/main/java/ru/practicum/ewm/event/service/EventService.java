package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.request.service.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

@Service
public interface EventService {
    List<EventFullDto> getAllAdmin(EventAdminParams eventAdminParams);

    EventFullDto updateAdmin(Long eventId, UpdateEventAdminRequest updateEvent);

    List<EventShortDto> getByUserId(Long userId, Integer from, Integer size);

    EventFullDto create(Long userId, NewEventDto newEventDto);

    EventFullDto getByUserIdAndEventId(Long userId, Long eventId);

    EventFullDto updateByUserIdAndEventId(Long userId, Long eventId, UpdateEventUserRequest eventUpdate);

    List<ParticipationRequestDto> getAllParticipationRequestsByOwner(Long userId, Long eventId);

    List<EventShortDto> getAllPublic(EventParams eventParams, HttpServletRequest request);

    EventFullDto getById(Long eventId, HttpServletRequest request);

    Map<String, List<ParticipationRequestDto>> approveRequests(final Long userId, final Long eventId,
                                                               final EventRequestStatusUpdateRequest requestUpdateDto);

}