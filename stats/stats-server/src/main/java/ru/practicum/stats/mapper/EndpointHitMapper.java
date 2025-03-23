package ru.practicum.stats.mapper;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.model.Hit;

/**
 * Маппер DTO <--> model
 */
public final class EndpointHitMapper {
    public static Hit toEndpointHit(EndpointHitDto hit) {
        return Hit.builder()
                .app(hit.getApp())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .uri(hit.getUri())
                .build();
    }

    public static EndpointHitDto toEndpointHitDto(Hit hit) {
        return new EndpointHitDto(
                hit.getId(),
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp()
        );
    }
}
