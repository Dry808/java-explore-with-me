package ru.practicum.ewm.event.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.event.enums.SortOption;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventFilterDto {
    private String text;
    private List<Long> categories;
    private Boolean paid;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    private Boolean onlyAvailable = false;
    private SortOption sort;
    private Integer from = 0;
    private Integer size = 10;
}
