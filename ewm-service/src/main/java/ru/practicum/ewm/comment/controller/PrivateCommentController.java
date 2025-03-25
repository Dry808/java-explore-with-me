package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import java.util.List;

/**
 * Контроллер для комментариев
 */
@Slf4j
@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService service;

    @GetMapping
    public List<CommentDto> getEventComments(@RequestParam Long eventId,
                                             @RequestParam(defaultValue = "0", required = false) Integer from,
                                             @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("Получение комментариев для события ID={} (from={}, size={})", eventId, from, size);
        return service.getEventComments(eventId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long userId,
                                 @RequestParam Long eventId,
                                 @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Создание нового комментария к событию ID={} пользователем ID={}", eventId, userId);
        return service.createComment(userId, eventId, newCommentDto);
    }

    @PatchMapping
    public CommentDto updateComment(@PathVariable Long userId,
                                    @RequestParam Long eventId,
                                    @RequestBody @Valid UpdateCommentDto comment) {
        log.info("Обновление комментария ID={} пользователем ID={} для события ID={}",
                comment.getCommentId(), userId, eventId);
        return service.updateComment(userId, eventId, comment);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long userId,
                              @RequestParam Long eventId,
                              @PathVariable Long commentId) {
        log.info("Удаление комментария ID={} пользователем ID={} для события ID={}", commentId, userId, eventId);
        service.deleteComment(userId, eventId, commentId);
    }
}
