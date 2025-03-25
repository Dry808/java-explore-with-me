package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.storage.CompilationRepository;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.event.storage.EventRepository;

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

    // получить список подборок с учётом фильтров
    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {
        if (pinned != null) {
            return compilationRepository.findByPinned(pinned).stream()
                    .skip(from)
                    .limit(size)
                    .map(CompilationMapper::toDto)
                    .toList();
        } else {
            return compilationRepository.findAll().stream()
                    .skip(from)
                    .limit(size)
                    .map(CompilationMapper::toDto)
                    .toList();
        }
    }

    // получить по Id
    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long compId) {
        return CompilationMapper.toDto(compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка с id=%d не найдена".formatted(compId))
        ));
    }

    // создать подборку
    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilation) {
        List<Long> uniqueEvents = Optional.ofNullable(newCompilation.getEvents())
                .orElse(List.of())
                .stream()
                .distinct() // убрать дубликаты
                .toList();

        // получить события по Id
        List<Event> events = eventRepository.findByIdIn(uniqueEvents);

        // проверкаа событий
        if (uniqueEvents.size() != events.size()) {
            throw new NotFoundException("Некоторые события не найдены");
        }

        // создать новую подборку
        Compilation compilation = Compilation.builder()
                .title(newCompilation.getTitle())
                .pinned(Optional.ofNullable(newCompilation.getPinned()).orElse(false))
                .events(events)
                .build();

        return CompilationMapper.toDto(compilationRepository.save(compilation));
    }

    // обновить подборку
    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest compilation) {
        Compilation oldCompilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка с id=%d не найдена".formatted(compId)) // проверка
        );

        // изменить pinned если обновился
        if (compilation.getPinned() != null && compilation.getPinned() != oldCompilation.getPinned()) {
            oldCompilation.setPinned(compilation.getPinned());
        }

        // получает уникальные ID событий из запроса
        List<Long> uniqueEvents = Optional.ofNullable(compilation.getEvents())
                .orElse(List.of())
                .stream()
                .distinct()
                .toList();

        List<Event> eventsList = eventRepository.findByIdIn(uniqueEvents);

        // проверка, что все события найдены
        if (uniqueEvents.size() != eventsList.size()) {
            throw new NotFoundException("Некоторые events не найдены");
        }

        Set<Event> events = new HashSet<>(eventsList);
        Set<Event> oldEvents = new HashSet<>(oldCompilation.getEvents());

        if (!oldEvents.equals(events)) {
            oldCompilation.setEvents(eventsList);
        }

        // заголовки
        if (compilation.getTitle() != null && !oldCompilation.getTitle().equals(compilation.getTitle())) {
            oldCompilation.setTitle(compilation.getTitle());
        }

        return CompilationMapper.toDto(compilationRepository.save(oldCompilation));
    }

    // удалить по id
    @Override
    public void deleteById(Long compId) {
        try {
            compilationRepository.deleteById(compId);
        } catch (EmptyResultDataAccessException ex) {
            throw new NotFoundException("Подборка с id=%d не найдена".formatted(compId));
        }
    }
}
