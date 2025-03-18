package ru.practicum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.StatOutDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository  extends JpaRepository<Statistics, Long> {
    @Query("""
            SELECT new ru.practicum.StatOutDto(s.ip, s.uri, COUNT(DISTINCT s.ip))
            FROM Statistics AS s
            WHERE s.timestamp BETWEEN :start AND :end AND s.uri IN :uris
            GROUP BY s.ip, s.uri
            ORDER BY COUNT(DISTINCT s.ip) DESC
            """)
    List<StatOutDto> findAllWithUniqueIpWithUris(List<String> uris,
                                                 LocalDateTime start,
                                                 LocalDateTime end);

    @Query("""
            SELECT new ru.practicum.StatOutDto(s.ip, s.uri, COUNT(DISTINCT s.ip))
            FROM Statistics AS s
            WHERE s.timestamp BETWEEN :start AND :end
            GROUP BY s.ip, s.uri
            ORDER BY COUNT(DISTINCT s.ip) DESC
            """)
    List<StatOutDto> findAllWithUniqueIpWithoutUris(LocalDateTime start,
                                                    LocalDateTime end);

    @Query("""
            SELECT new ru.practicum.StatOutDto(s.ip, s.uri, COUNT(s.ip))
            FROM Statistics AS s
            WHERE s.timestamp BETWEEN :start AND :end AND s.uri IN :uris
            GROUP BY s.ip, s.uri
            ORDER BY COUNT (s.ip) DESC
            """)
    List<StatOutDto> findAllWithUris(List<String> uris,
                                     LocalDateTime start,
                                     LocalDateTime end);

    @Query("""
            SELECT new ru.practicum.StatOutDto(s.ip, s.uri, COUNT(s.ip))
            FROM Statistics AS s
            WHERE s.timestamp BETWEEN :start AND :end
            GROUP BY s.ip, s.uri
            ORDER BY COUNT (s.ip) DESC
            """)
    List<StatOutDto> findAllWithoutUris(LocalDateTime start,
                                        LocalDateTime end);
}