package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.participation.dto.ParticipationRequestDto;
import ru.practicum.participation.service.ParticipationService;

import java.util.List;

/**
 * Контроллер Events приватный
 */
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {
    private final EventService service;
    private final ParticipationService participationService;

    @GetMapping
    public List<EventShortDto> getPrivateAll(@PathVariable("userId") Long userId,
                                             @RequestParam(defaultValue = "0", required = false) Integer from,
                                             @RequestParam(defaultValue = "10", required = false) Integer size) {
        return service.getAll(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getOne(@PathVariable("userId") Long userId,
                               @PathVariable("eventId") Long eventId) {
        return service.getById(userId, eventId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public EventFullDto create(@PathVariable("userId") Long userId,
                               @RequestBody NewEventDto eventDto) {
        return service.create(userId, eventDto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable("userId") Long userId,
                               @PathVariable("eventId") Long eventId,
                               @RequestBody @Valid UpdateEventUserRequest request) {
        return service.update(userId, eventId, request);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable("userId") Long userId,
                                                     @PathVariable("eventId") Long eventId) {
        return participationService.getRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult setRequestsStatusResults(@PathVariable("userId") Long userId,
                                                                   @PathVariable("eventId") Long eventId,
                                                                   @RequestBody EventRequestStatusUpdateRequest request) {
        return participationService.setRequestsStatusResults(userId, eventId, request);
    }
}
