package ru.practicum.stats.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.mapper.EndpointHitMapper;
import ru.practicum.stats.storage.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для работы со статистикой посещений эндпоинтов.
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public void addHit(EndpointHitDto hit) {
        statsRepository.save(EndpointHitMapper.toEndpointHit(hit));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end) || start.isAfter(LocalDateTime.now())) {
            throw new ValidationException("Диапазон статистики указан некорректно");
        }
        if (unique) {
            if (uris != null && !uris.isEmpty()) {
                return statsRepository.getUniqueStatsForUris(start, end, uris);
            } else {
                return statsRepository.getUniqueStats(start, end);
            }
        } else {
            if (uris != null && !uris.isEmpty()) {
                return statsRepository.getStatsForUris(start, end, uris);
            } else {
                return statsRepository.getStats(start, end);
            }
        }
    }
}
