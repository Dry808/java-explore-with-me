package ru.practicum.ewm.event.mapper;

import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.user.mapper.UserMapper;

/**
 * Mapper Event <--> DTO
 */
public class EventMapper {

    public static EventFullDto toDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .category(CategoryMapper.toDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toShortDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .state(event.getState())
                .requestModeration(event.getRequestModeration())
                .build();
    }

    public static EventShortDto toShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .initiator(UserMapper.toShortDto(event.getInitiator()))
                .eventDate(event.getEventDate())
                .confirmedRequests(event.getConfirmedRequests())
                .annotation(event.getAnnotation())
                .paid(event.getPaid())
                .category(CategoryMapper.toDto(event.getCategory()))
                .build();
    }
}
