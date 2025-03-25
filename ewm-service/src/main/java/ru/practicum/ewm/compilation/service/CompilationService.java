package ru.practicum.ewm.compilation.service;

import jakarta.validation.Valid;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size);

    CompilationDto getById(Long compId);

    CompilationDto create(NewCompilationDto newCompilation);

    CompilationDto update(Long compId, @Valid UpdateCompilationRequest compilation);

    void deleteById(Long compId);
}
