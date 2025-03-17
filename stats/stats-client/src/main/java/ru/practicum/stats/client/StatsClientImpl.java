package ru.practicum.stats.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import org.springframework.web.client.RestTemplate;

import org.springframework.web.util.UriComponentsBuilder;
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
    public ResponseEntity<Object> getStats(String start, String end, List<String> uris, Boolean unique) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", start)
                .queryParam("end", end);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", String.join(",", uris));
        }

        if (unique != null) {
            builder.queryParam("unique", unique);
        }

        return rest.exchange(builder.toUriString(), HttpMethod.GET, null, Object.class);
    }
}
