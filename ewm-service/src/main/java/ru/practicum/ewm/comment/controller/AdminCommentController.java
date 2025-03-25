package ru.practicum.ewm.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.service.CommentService;

/**
 * Контроллер для работы с комментариями для админов
 */
@Slf4j
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService service;

    @PatchMapping("/{commentId}/moderate")
    @ResponseStatus(HttpStatus.OK)
    public void moderateComment(@PathVariable Long commentId) {
        log.info("Модерация комментария с ID" + commentId);
        service.moderateComment(commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.info("Удаления комментария админом с ID" + commentId);
        service.deleteComment(commentId);
    }
}