package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.AdminEventFilterDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.service.EventService;

import java.util.List;

/**
 * Контроллер Events для админов
 */
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final EventService service;

    @GetMapping
    public List<EventFullDto> getAll(@ModelAttribute AdminEventFilterDto filterDto) {
        return service.getAll(filterDto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable("eventId") Long eventId,
                               @RequestBody @Valid UpdateEventAdminRequest request) {
        return service.update(eventId, request);
    }
}
