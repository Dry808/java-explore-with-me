package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.constants.DateFormatConstants;
import ru.practicum.ewm.location.Location;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @NotBlank
    @Length(max = 2000, min = 20)
    String annotation;

    @NotNull
    @Positive
    Long category;

    @NotBlank
    @Length(max = 7000, min = 20)
    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatConstants.DATE_TIME_PATTERN)
    LocalDateTime eventDate;

    @NotNull
    @Valid
    Location.LocationDto location;

    private boolean paid;

    @PositiveOrZero
    int participantLimit;
    boolean requestModeration = true;

    @NotNull
    @Length(min = 3, max = 120)
    String title;
}