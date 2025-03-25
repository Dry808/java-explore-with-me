package ru.practicum.ewm.comment.service;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    List<CommentDto> getEventComments(Long eventId, Integer from, Integer size);

    CommentDto updateComment(Long userId, Long eventId, UpdateCommentDto comment);

    void moderateComment(Long commentId);

    void deleteComment(Long commentId);

    void deleteComment(Long userId, Long eventId, Long commentId);
}
