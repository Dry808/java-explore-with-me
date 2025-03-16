package ru.practicum.compilation.mapper;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;

public final class CompilationMapper {
    public static CompilationDto toDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .events(compilation.getEvents().stream().map(EventMapper::toShortDto).toList())
                .pinned(compilation.getPinned())
                .build();
    }
}
