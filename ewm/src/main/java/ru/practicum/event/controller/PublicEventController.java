package ru.practicum.event.controller;


import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import ru.practicum.event.service.EventService;
import ru.practicum.stats.client.StatsClient;


@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService service;
    private final StatsClient statsClient;


}
