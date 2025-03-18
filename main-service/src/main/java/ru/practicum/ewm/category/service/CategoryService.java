package ru.practicum.ewm.category.service;

import ru.practicum.ewm.category.dto.CategoryInDto;
import ru.practicum.ewm.category.dto.CategoryOutDto;

import java.util.List;

public interface CategoryService {
    CategoryOutDto getById(Long id);

    List<CategoryOutDto> get(Integer from, Integer size);

    CategoryOutDto create(CategoryInDto categoryInDto);

    void delete(Long id);

    CategoryOutDto update(Long id,CategoryInDto categoryInDto);

}