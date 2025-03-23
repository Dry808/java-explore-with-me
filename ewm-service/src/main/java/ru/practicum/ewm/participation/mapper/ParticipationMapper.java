package ru.practicum.ewm.participation.mapper;


import ru.practicum.ewm.participation.model.Participation;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;

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
