package ru.practicum.event.mapper;

import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;

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
