package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.event.dto.EventAdminParams;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events")
public class EventAdminController {
    private final EventService eventService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    public List<EventFullDto> getAll(
            @RequestParam(required = false) final List<Long> users,
            @RequestParam(required = false) final List<String> states,
            @RequestParam(required = false) final List<Long> categories,
            @RequestParam(required = false) final String rangeStart,
            @RequestParam(required = false) final String rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero final int from,
            @RequestParam(defaultValue = "10") @Positive final int size) {
        log.info("==> ЗАПОРС на получение всех событий (ADMIN)");
        LocalDateTime start = (rangeStart != null) ? LocalDateTime.parse(rangeStart, formatter) : LocalDateTime.now();
        LocalDateTime end = (rangeEnd != null) ? LocalDateTime.parse(rangeEnd, formatter) : LocalDateTime.now().plusYears(20);
        EventAdminParams eventAdminParams =  new EventAdminParams();
        eventAdminParams.setUsers(users);
        eventAdminParams.setStates(states);
        eventAdminParams.setCategories(categories);
        eventAdminParams.setRangeStart(start);
        eventAdminParams.setRangeEnd(end);
        eventAdminParams.setFrom(from);
        eventAdminParams.setSize(size);
        return eventService.getAllAdmin(eventAdminParams);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable(value = "eventId") @Min(1) Long eventId,
                               @RequestBody @Valid UpdateEventAdminRequest inputUpdate) {

        log.info("==> ЗАПОРС на обновление списка событий");
        return eventService.updateAdmin(eventId, inputUpdate);
    }
}