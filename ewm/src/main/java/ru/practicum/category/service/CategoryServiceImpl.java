package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.dto.UpdateCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.storage.CategoryRepository;
import ru.practicum.event.storage.EventRepository;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;

import java.util.List;

/**
 * Сервис для работы с категориями
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;
    private final EventRepository eventRepository;

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {
        return repository.findAll().stream()
                .skip(from)
                .limit(size)
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getById(Long catId) {
        return CategoryMapper.toDto(repository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория id=%d не найден".formatted(catId))
        ));
    }

    @Override
    public CategoryDto create(NewCategoryDto category) {
        return CategoryMapper.toDto(repository.save(Category.builder()
                .name(category.getName())
                .build()));
    }

    @Override
    public CategoryDto update(UpdateCategoryDto dto, Long catId) {
        Category category = repository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория c id=%d не найден".formatted(catId)));

        if (!dto.getName().equals(category.getName())) {
            category.setName(dto.getName());
        }
        Category savedCategory = repository.save(category);
        repository.flush();
        return CategoryMapper.toDto(savedCategory);
    }

    @Override
    public void deleteById(Long catId) {
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Категория не пустая");
        }
        repository.deleteById(catId);
    }
}
