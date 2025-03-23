package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exceptions.ConflictException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.dto.UpdateCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.storage.CategoryRepository;
import ru.practicum.ewm.event.storage.EventRepository;

import java.util.List;

/**
 * Сервис для работы с категориями
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;
    private final EventRepository eventRepository;

    // получить категории
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAll(Integer from, Integer size) {
        return repository.findAll().stream()
                .skip(from)
                .limit(size)
                .map(CategoryMapper::toDto)
                .toList();
    }

    // получить по ID
    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(Long catId) {
        return CategoryMapper.toDto(repository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория с id=%d не найдена".formatted(catId))
        ));
    }

    // создать категорию
    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto category) {
        return CategoryMapper.toDto(repository.save(Category.builder()
                .name(category.getName())
                .build()));
    }

    // обновить категорию
    @Override
    @Transactional
    public CategoryDto update(UpdateCategoryDto dto, Long catId) {
        Category category = repository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=%d не найдена".formatted(catId)));

        if (!dto.getName().equals(category.getName())) { // обновить имя если измениллось
            category.setName(dto.getName());
        }
        Category savedCategory = repository.save(category);
        repository.flush(); // изменить в БД
        return CategoryMapper.toDto(savedCategory);
    }

    // удалить категорию по Id
    @Override
    @Transactional
    public void deleteById(Long catId) {
        if (eventRepository.existsByCategoryId(catId)) { // проверка event связанных с категорией
            throw new ConflictException("Категория не пустая");
        }
        repository.deleteById(catId);
    }
}
