package ru.practicum.ewm.category.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryInDto {
    @NotBlank
    @Size(max = 50, message = "Максимальная длина - 50 символов")
    String name;
}