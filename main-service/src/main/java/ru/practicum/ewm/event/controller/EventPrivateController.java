package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.request.service.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/users/{userId}/events")
public class EventPrivateController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllEventsByUserId(@PathVariable(value = "userId") @Min(1) Long userId,
                                                    @RequestParam(value = "from", defaultValue = "0")
                                                    @PositiveOrZero Integer from,
                                                    @RequestParam(value = "size", defaultValue = "10")
                                                    @Positive Integer size) {
        log.info("==> Запрос на получения событий пользователя с id= {}", userId);
        return eventService.getByUserId(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable(value = "userId") @Min(1) Long userId,
                                 @RequestBody @Valid NewEventDto input) {
        log.info("==> Cоздание события от пользователя с id= {}", userId);
        EventFullDto eventFullDto = eventService.create(userId, input);
        log.info("<== Cобытия от пользователя с id= {} создано", userId);
        return eventFullDto;
    }

    @GetMapping("/{eventId}")
    public EventFullDto getFullEventByOwner(@PathVariable(value = "userId") @Min(1) Long userId,
                                            @PathVariable(value = "eventId") @Min(1) Long eventId) {
        log.info("==> Запрос на получения полной информации о событии для пользователя с id= {}", userId);
        return eventService.getByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByOwner(@PathVariable(value = "userId") @Min(0) Long userId,
                                           @PathVariable(value = "eventId") @Min(0) Long eventId,
                                           @RequestBody @Valid UpdateEventUserRequest inputUpdate) {
        log.info("==> Запрос на обновление события от пользователя с id= {}", userId);
        return eventService.updateByUserIdAndEventId(userId, eventId, inputUpdate);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getAllRequestByEventFromOwner(@PathVariable(value = "userId") @Min(1) Long userId,
                                                                       @PathVariable(value = "eventId") @Min(1) Long eventId) {
        log.info("==> Запрос на получение информации о всех запросах об участии в событии для пользователя с id= {}", userId);
        return eventService.getAllParticipationRequestsByOwner(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public Map<String, List<ParticipationRequestDto>> approveRequests(@PathVariable final Long userId,
                                                                      @PathVariable final Long eventId,
                                                                      @RequestBody @Valid final EventRequestStatusUpdateRequest requestUpdateDto) {
        return eventService.approveRequests(userId, eventId, requestUpdateDto);
    }
}