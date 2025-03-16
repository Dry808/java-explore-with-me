package ru.practicum.stats.mapper;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.model.EndpointHit;

/**
 * Маппер DTO <--> model
 */

public final class EndpointHitMapper {
    public static EndpointHit toEndpointHit(EndpointHitDto hit) {
        return new EndpointHit(
                hit.getId(),
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp()
        );
    }

    public static EndpointHitDto toEndpointHitDto(EndpointHit hit) {
        return new EndpointHitDto(
                hit.getId(),
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp()
        );
    }
}
