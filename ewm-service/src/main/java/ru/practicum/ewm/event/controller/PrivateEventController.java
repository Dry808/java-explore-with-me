package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.participation.service.ParticipationService;

import java.util.List;

/**
 * Контроллер для управления событиями (admin)
 */
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class PrivateEventController {
    private final EventService service;
    private final ParticipationService participationService;

    // получить список всеъ пользователей с пагинацией
    @GetMapping
    public List<EventShortDto> getPrivateAll(@PathVariable("userId") Long userId,
                                             @RequestParam(defaultValue = "0", required = false) Integer from,
                                             @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("Запрос на получение списка событий пользователя с ID={}, from={}, size={}", userId, from, size);
        return service.getAll(userId, from, size);
    }

    // Получить событие пользователя по ID
    @GetMapping("/{eventId}")
    public EventFullDto getOne(@PathVariable("userId") Long userId,
                               @PathVariable("eventId") Long eventId) {
        log.info("Запрос на получение события с ID={} для пользователя с ID={}", eventId, userId);
        return service.getById(userId, eventId);
    }

    // создать
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable("userId") Long userId, @RequestBody @Valid NewEventDto eventDto) {
        log.info("Создание события");
        return service.create(userId, eventDto);
    }

    // обновить event
    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable("userId") Long userId,
                               @PathVariable("eventId") Long eventId,
                               @RequestBody @Valid UpdateEventUserRequest request) {
        log.info("Запрос на обновление события с ID={} пользователем ID={}", eventId, userId);
        return service.update(userId, eventId, request);
    }

    // Получить запросы
    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable("userId") Long userId,
                                                     @PathVariable("eventId") Long eventId) {
        log.info("Получение запросов событие = ID={} , пользователь ID={}", eventId, userId);
        return participationService.getRequests(userId, eventId);
    }

    // Обновить статусы запросов на участие в событии
    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult setRequestsStatusResults(@PathVariable("userId") Long userId,
                                                                   @PathVariable("eventId") Long eventId,
                                                                   @RequestBody EventRequestStatusUpdateRequest request) {
        return participationService.setRequestsStatusResults(userId, eventId, request);
    }
}
