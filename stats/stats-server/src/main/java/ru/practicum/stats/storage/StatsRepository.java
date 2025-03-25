package ru.practicum.stats.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(h) AS hits  \n" +
            "FROM Hit h\n" +
            "where h.timestamp between :start and :end\n" +
            "GROUP BY h.app, h.uri\n" +
            "ORDER BY hits DESC")
    List<ViewStatsProjection> getAllViewStatsProjection(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(h) AS hits  \n" +
            "FROM Hit h\n" +
            "where (h.timestamp between :start and :end) and uri in :uris\n" +
            "GROUP BY h.app, h.uri\n" +
            "ORDER BY hits DESC")
    List<ViewStatsProjection> getViewStatsProjectionByUris(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("uris") List<String> uris);

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(DISTINCT h.ip) AS hits  \n" +
            "FROM Hit h\n" +
            "where h.timestamp between :start and :end\n" +
            "GROUP BY h.app, h.uri\n" +
            "ORDER BY hits DESC")
    List<ViewStatsProjection> getViewStatsProjectionUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(DISTINCT h.ip) AS hits  \n" +
            "FROM Hit h\n" +
            "where (h.timestamp between :start and :end) and uri in :uris\n" +
            "GROUP BY h.app, h.uri\n" +
            "ORDER BY hits DESC")
    List<ViewStatsProjection> getViewStatsProjectionByUrisUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("uris") List<String> uris);
}
