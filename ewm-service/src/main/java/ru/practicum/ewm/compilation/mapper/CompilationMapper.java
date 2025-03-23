package ru.practicum.ewm.compilation.mapper;

import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.dto.CompilationDto;

/**
 * Mapper compilation <-->DTO
 */
public class CompilationMapper {
    public static CompilationDto toDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .events(compilation.getEvents().stream().map(EventMapper::toShortDto).toList())
                .pinned(compilation.getPinned())
                .build();
    }
}
