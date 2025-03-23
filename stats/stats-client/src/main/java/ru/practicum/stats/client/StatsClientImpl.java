package ru.practicum.stats.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Реализация клиента для взаимодействия с сервисом статистики
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class StatsClientImpl implements StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RestTemplate rest;
    @Value("${spring.application.name}")
    private String applicationName;

    // Метод для отправки информации о посещении эндпоинта
    @Override
    public ResponseEntity<Void> addHit(String uri, String ip) {
        EndpointHitDto hit = EndpointHitDto.builder()
                .app(applicationName)
                .timestamp(LocalDateTime.now())
                .uri(uri)
                .ip(ip)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHitDto> request = new HttpEntity<>(hit, headers);

        return rest.exchange("/hit", HttpMethod.POST, request, Void.class);
    }

    // Метод для получения статистики просмотров
    @Override
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
