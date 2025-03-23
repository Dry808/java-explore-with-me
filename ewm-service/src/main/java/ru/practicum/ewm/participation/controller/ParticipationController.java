package ru.practicum.ewm.participation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.service.ParticipationService;

import java.util.List;

/**
 * Контроллер для работы с участниками (в событиях)
 */
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Slf4j
public class ParticipationController {
    private final ParticipationService service;

    @GetMapping
    public List<ParticipationRequestDto> getRequestsByUserId(@PathVariable("userId") Long userId) {
        log.info("Получение запросов на участие для пользователя с ID: {}", userId);
        return service.getRequestsByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable("userId") Long userId, @RequestParam Long eventId) {
        log.info("Создание запроса на участие для пользователя с ID: {} и события с ID: {}", userId, eventId);
        return service.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable("userId") Long userId,
                                                 @PathVariable("requestId") Long requestId) {
        return service.cancelRequest(userId, requestId);
    }
}
