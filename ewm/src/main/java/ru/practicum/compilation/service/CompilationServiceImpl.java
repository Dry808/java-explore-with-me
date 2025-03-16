package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.storage.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.storage.EventRepository;
import ru.practicum.exceptions.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Сервис для работы с подборками
 */
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    //Получить список всех подборок событий с возможностью фильтрации
    @Override
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {
        if (pinned != null) {
            return compilationRepository.findByPinned(pinned).stream()
                    .skip(from)
                    .limit(size)
                    .map(CompilationMapper::toDto)
                    .toList();
        } else { // иначе возвращаем все подборки
            return compilationRepository.findAll().stream()
                    .skip(from)
                    .limit(size)
                    .map(CompilationMapper::toDto)
                    .toList();
        }
    }

    // получить подборку по ID
    @Override
    public CompilationDto getById(Long compId) {
        return CompilationMapper.toDto(compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Compilation c id=%d не найден".formatted(compId))
        ));
    }

    // создать новую подборку событий
    @Override
    public CompilationDto create(NewCompilationDto newCompilation) {
        // Убираем дубликаты ID событий
        List<Long> uniqueEvents = Optional.ofNullable(newCompilation.getEvents())
                .orElse(List.of())
                .stream()
                .distinct()
                .toList();
        //получаем события по Id и проверяем
        List<Event> events = eventRepository.findByIdIn(uniqueEvents);
        if (uniqueEvents.size() != events.size()) {
            throw new NotFoundException("Event не найдены");
        }
        // создаём подборку
        Compilation compilation = Compilation.builder()
                .title(newCompilation.getTitle())
                .pinned(Optional.ofNullable(newCompilation.getPinned()).orElse(false)) // по умолчанию false
                .events(events)
                .build();

        return CompilationMapper.toDto(compilationRepository.save(compilation));
    }

    // Обновить существующую подборку
    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest compilation) {
        // получаем подборку по id
        Compilation oldCompilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Compilation с id=%d не найден".formatted(compId))
        );
        // обновляем pinned
        if (compilation.getPinned() != null && compilation.getPinned() != oldCompilation.getPinned()) {
            oldCompilation.setPinned(compilation.getPinned());
        }

        // убираем дубликаты
        List<Long> uniqueEvents = Optional.ofNullable(compilation.getEvents())
                .orElse(List.of())
                .stream()
                .distinct()
                .toList();

        List<Event> eventsList = eventRepository.findByIdIn(uniqueEvents);
        if (uniqueEvents.size() != eventsList.size()) {
            throw new NotFoundException("Событие не найдены");
        }
        // Преобразуем в set для сравнения
        Set<Event> events = new HashSet<>(eventsList);
        Set<Event> oldEvents = new HashSet<>(oldCompilation.getEvents());

        // Обновляем список событий, если он изменился
        if (!oldEvents.equals(events)) {
            oldCompilation.setEvents(eventsList);
        }

        // Обновляем название подборки
        if (compilation.getTitle() != null && !oldCompilation.getTitle().equals(compilation.getTitle())) {
            oldCompilation.setTitle(compilation.getTitle());
        }

        return CompilationMapper.toDto(compilationRepository.save(oldCompilation));
    }

    // удалить подборку по id
    @Override
    public void deleteById(Long compId) {
        try {
            compilationRepository.deleteById(compId);
        } catch (NotFoundException ex) {
            throw new NotFoundException("Compilation с id=%d не найден".formatted(compId));
        }
    }
}
