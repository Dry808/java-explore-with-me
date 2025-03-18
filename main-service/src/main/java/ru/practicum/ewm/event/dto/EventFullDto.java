package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.category.dto.CategoryOutDto;
import ru.practicum.ewm.constants.DateFormatConstants;
import ru.practicum.ewm.event.model.EventStatus;
import ru.practicum.ewm.location.Location;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDto {
    Long id;

    String annotation;

    CategoryOutDto category;

    Integer confirmedRequests;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatConstants.DATE_TIME_PATTERN)
    LocalDateTime createdOn;

    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatConstants.DATE_TIME_PATTERN)
    LocalDateTime eventDate;

    UserShortDto initiator;

    Location.LocationDto location;

    Boolean paid;

    Integer participantLimit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatConstants.DATE_TIME_PATTERN)
    LocalDateTime publishedOn;

    Boolean requestModeration;

    EventStatus state;

    String title;

    Long views;
}