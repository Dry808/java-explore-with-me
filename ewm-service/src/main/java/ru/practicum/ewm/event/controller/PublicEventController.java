package ru.practicum.ewm.event.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFilterDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.stats.client.StatsClient;

import java.util.List;

/**
 * контроллер для работы с events (public)
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService service;
    private final StatsClient statsClient;

    // получить список событий
    @GetMapping
    public List<EventShortDto> getAll(@ModelAttribute EventFilterDto filterDto, HttpServletRequest request) {
        statsClient.addHit(request.getRequestURI(), request.getRemoteAddr());
        return service.getAll(filterDto);
    }

    // получить по id
    @GetMapping("/{id}")
    public EventFullDto getOne(@PathVariable("id") Long id, HttpServletRequest request) {
        statsClient.addHit(request.getRequestURI(), request.getRemoteAddr());
        return service.getById(id, request.getRemoteAddr());
    }
}
