package ru.practicum.ewm.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private Long eventId;
    private Long authorId;
    private String text;
    private LocalDateTime createdOn;
    private Boolean isModerated;
}
