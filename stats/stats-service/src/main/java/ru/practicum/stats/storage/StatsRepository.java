package ru.practicum.stats.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new ru.practicum.stats.dto.ViewStatsDto(hit.app, hit.uri, COUNT(hit.ip)) FROM EndpointHit hit " +
            "WHERE (hit.timestamp BETWEEN :start AND :end) GROUP BY hit.app, hit.uri ORDER BY COUNT(hit.ip) DESC")
    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.stats.dto.ViewStatsDto(hit.app, hit.uri, COUNT(hit.ip)) FROM EndpointHit hit " +
            "WHERE (hit.timestamp BETWEEN :start AND :end) AND (hit.uri IN :uris) GROUP BY hit.app, hit.uri " +
            "ORDER BY COUNT(hit.ip) DESC")
    List<ViewStatsDto> getStatsForUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.stats.dto.ViewStatsDto(hit.app, hit.uri, COUNT(DISTINCT hit.ip)) FROM EndpointHit hit " +
            "WHERE (hit.timestamp BETWEEN :start AND :end) GROUP BY hit.app, hit.uri ORDER BY COUNT(DISTINCT hit.ip) DESC")
    List<ViewStatsDto> getUniqueStats(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.stats.dto.ViewStatsDto(hit.app, hit.uri, COUNT(DISTINCT hit.ip)) FROM EndpointHit hit " +
            "WHERE (hit.timestamp BETWEEN :start AND :end) AND (hit.uri IN :uris) GROUP BY hit.app, hit.uri " +
            "ORDER BY COUNT(DISTINCT hit.ip) DESC")
    List<ViewStatsDto> getUniqueStatsForUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}