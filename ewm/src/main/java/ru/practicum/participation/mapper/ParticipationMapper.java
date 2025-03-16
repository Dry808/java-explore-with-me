package ru.practicum.participation.mapper;

import ru.practicum.participation.dto.ParticipationRequestDto;
import ru.practicum.participation.model.Participation;

public class ParticipationMapper {

    public static ParticipationRequestDto toDto(Participation participation) {
        return ParticipationRequestDto.builder()
                .id(participation.getId())
                .event(participation.getEvent().getId())
                .requester(participation.getRequester().getId())
                .status(participation.getStatus())
                .created(participation.getCreated())
                .build();
    }
}
