package ru.practicum.service;

import ru.practicum.StatDto;
import ru.practicum.StatInDto;
import ru.practicum.StatOutDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {
    StatDto create(StatInDto statInDto);

    List<StatOutDto> get(LocalDateTime start,
                         LocalDateTime end,
                         List<String> uris,
                         Boolean unique);
}