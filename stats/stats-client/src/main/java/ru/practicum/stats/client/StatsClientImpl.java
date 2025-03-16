package ru.practicum.stats.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import org.springframework.web.client.RestTemplate;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Реализация клиента для взаимодействия с сервисом статистики
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class StatsClientImpl implements StatsClient {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final RestTemplate rest;
    @Value("${spring.application.name}")
    private String appName;

    // Метод для отправки информации о посещении эндпоинта
    @Override
    public void addHit(String uri, String ip) {
        EndpointHitDto hit = EndpointHitDto.builder()
                .app(appName)
                .timestamp(LocalDateTime.now())
                .uri(uri)
                .ip(ip)
                .build();
        rest.postForObject("/hit", hit, ResponseEntity.class);
    }

    // Метод для получения статистики просмотров
    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        Map<String, Object> uriVariables = Map.of(
                "start", start.format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                "end", end.format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                "uris", uris, "unique", unique);
        ViewStatsDto[] response = rest.getForObject("/stats", ViewStatsDto[].class, uriVariables);
        return Arrays.stream(response).toList();
    }
}
