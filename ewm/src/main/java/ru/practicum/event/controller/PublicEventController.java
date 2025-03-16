package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFilterDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;
import ru.practicum.stats.client.StatsClient;

import java.util.List;

/**
 * Контроллер Events публичный
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService service;
    private final StatsClient statsClient;

    @GetMapping
    public List<EventShortDto> getAll(@ModelAttribute EventFilterDto filterDto, HttpServletRequest request) {
        statsClient.addHit(request.getRequestURI(), request.getRemoteAddr());
        return service.getAll(filterDto);
    }

    @GetMapping("/{id}")
    public EventFullDto getById(@PathVariable("id") Long id, HttpServletRequest request) {
        statsClient.addHit(request.getRequestURI(), request.getRemoteAddr());
        return service.getById(id, request.getRemoteAddr());
    }
}
