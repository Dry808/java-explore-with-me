package ru.practicum.ewm.comment.mapper;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.model.Comment;

/**
 * Mapper comment <---> DTO
 */
public class CommentMapper {
    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthor().getId())
                .eventId(comment.getEvent().getId())
                .createdOn(comment.getCreatedOn())
                .isModerated(comment.getIsModerated())
                .build();
    }
}
