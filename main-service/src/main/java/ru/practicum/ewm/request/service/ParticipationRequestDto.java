package ru.practicum.ewm.request.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.constants.DateFormatConstants;
import ru.practicum.ewm.request.RequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequestDto {
    Long id;

    Long requester;

    Long event;

    RequestStatus status;

    @JsonFormat(pattern = DateFormatConstants.DATE_TIME_PATTERN)
    LocalDateTime created;
}