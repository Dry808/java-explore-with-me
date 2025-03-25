package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.exceptions.InvalidDateException;
import ru.practicum.stats.mapper.EndpointHitMapper;
import ru.practicum.stats.storage.StatsRepository;
import ru.practicum.stats.storage.ViewStatsProjection;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для работы со статистикой посещений эндпоинтов.
 */
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatsService {
    private final StatsRepository repository;

    @Override
    public void addHit(EndpointHitDto hit) {
        repository.save(EndpointHitMapper.toEndpointHit(hit));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<String> processedUris = (uris != null) ? uris : List.of();

        if (start.isAfter(end)) {
            throw new InvalidDateException("Диапазон статистики указан некорректно");
        }

        if (unique) {
            return processedUris.isEmpty()
                    ? getUniqueStats(start, end)
                    : getUniqueStatsByUris(start, end, processedUris);
        } else {
            return processedUris.isEmpty()
                    ? getAllStats(start, end)
                    : getStatsByUris(start, end, processedUris);
        }
    }

    // Получение общей статистики для всех uri
    private List<ViewStatsDto> getAllStats(LocalDateTime start, LocalDateTime end) {
        return convertProjections(repository.getAllViewStatsProjection(start, end));
    }

    // Получение общей статистики для указанных uri
    private List<ViewStatsDto> getStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return convertProjections(repository.getViewStatsProjectionByUris(start, end, uris));
    }

    // Получение уникальной статистики для всех uri
    private List<ViewStatsDto> getUniqueStats(LocalDateTime start, LocalDateTime end) {
        return convertProjections(repository.getViewStatsProjectionUnique(start, end));
    }

    // Получение уникальной статистики для указанных uri
    private List<ViewStatsDto> getUniqueStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return convertProjections(repository.getViewStatsProjectionByUrisUnique(start, end, uris));
    }

    // реобразование проекций в dto
    private List<ViewStatsDto> convertProjections(List<ViewStatsProjection> projections) {
        return projections.stream()
                .map(proj -> ViewStatsDto.builder()
                        .app(proj.getApp())
                        .uri(proj.getUri())
                        .hits(proj.getHits())
                        .build())
                .toList();
    }
}