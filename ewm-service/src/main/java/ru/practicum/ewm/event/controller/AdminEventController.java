package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.AdminEventFilterDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.service.EventService;

import java.util.List;

/**
 * Контроллер для управления событиями (admin)
 */
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final EventService service;

    // получить список событий с фильтрами
    @GetMapping
    public List<EventFullDto> getAll(@ModelAttribute AdminEventFilterDto filterDto) {
        return service.getAll(filterDto);
    }

    // обновить
    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable("eventId") Long eventId,
                               @RequestBody @Valid UpdateEventAdminRequest request) {
        return service.update(eventId, request);
    }
}
