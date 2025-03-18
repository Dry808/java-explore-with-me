package ru.practicum.ewm.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import ru.practicum.ewm.constants.DateFormatConstants;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ErrorAPI {
    private final List<String> errors;
    private final String message;
    private final String reason;
    private final String status;
    @JsonFormat(pattern = DateFormatConstants.DATE_TIME_PATTERN)
    private final LocalDateTime timestamp;
}