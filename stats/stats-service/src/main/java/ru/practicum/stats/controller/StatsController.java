package ru.practicum.stats.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Контроллер сервиса статистики
 */

@Slf4j
@RequiredArgsConstructor
@RestController
public class StatsController {
    private final StatsService statsService;

    // Сохранение информации о запросе
    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void addHit(@RequestBody @Valid EndpointHitDto hit) {
        log.info("Попытка добавления запроса: {}", hit);
        statsService.addHit(hit);
        log.info("Запрос на {} от {} сохранён", hit.getUri(), hit.getIp());
    }

    // получение статистики по посещениям
    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                       @RequestParam(defaultValue = "") List<String> uris,
                                       @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Запрошена статистика от {} до {}", start, end);
        return statsService.getStats(start, end, uris, unique);
    }
}
