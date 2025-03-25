package ru.practicum.stats.controller;


import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.exceptions.InvalidDateException;
import ru.practicum.stats.service.StatsService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Контроллер сервиса статистики
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
public class StatsController {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StatsService statsService;

    // Сохранение информации о запросе
    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void hit(@RequestBody @Valid EndpointHitDto hit) {
        log.info("Попытка добавления запроса: {}", hit);
        statsService.addHit(hit);
        log.info("Запрос на {} от {} сохранён", hit.getUri(), hit.getIp());
    }

    // получение статистики по посещениям
    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(@RequestParam @NonNull String start,
                                                       @RequestParam @NonNull String end,
                                                       @RequestParam(required = false, defaultValue = "") List<String> uris,
                                                       @RequestParam(defaultValue = "false") Boolean unique) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        try {
            String decodedStart = URLDecoder.decode(start, StandardCharsets.UTF_8);
            String decodedEnd = URLDecoder.decode(end, StandardCharsets.UTF_8);
            startDateTime = LocalDateTime.parse(decodedStart, FORMATTER);
            endDateTime = LocalDateTime.parse(decodedEnd, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidDateException("неправильный формат даты: " + e.getMessage());
        }
        log.info("Запрошена статистика от {} до {}", start, end);
        List<ViewStatsDto> results = statsService.getStats(startDateTime, endDateTime, uris, unique);
        return ResponseEntity.ok(results);
    }
}
