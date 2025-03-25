package ru.practicum.ewm.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для новых комментариев
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCommentDto {
    @NotBlank
    private String text;
}
