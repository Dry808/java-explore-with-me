package ru.practicum;

import ru.practicum.StatDto;
import ru.practicum.StatInDto;

public class StatisticsMapper {
    public static StatDto toStatDto(Statistics statistics) {
        return StatDto.builder()
                .app(statistics.getApp())
                .uri(statistics.getUri())
                .timestamp(statistics.getTimestamp())
                .build();
    }

    public static Statistics toStatistics(StatInDto statInDto) {
        return new Statistics(
                null,
                statInDto.getApp(),
                statInDto.getUri(),
                statInDto.getIp(),
                statInDto.getTimestamp()
        );
    }
}