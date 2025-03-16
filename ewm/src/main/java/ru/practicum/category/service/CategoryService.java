package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.dto.UpdateCategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getById(Long catId);

    CategoryDto create(NewCategoryDto category);

    CategoryDto update(UpdateCategoryDto category, Long catId);

    void deleteById(Long catId);
}