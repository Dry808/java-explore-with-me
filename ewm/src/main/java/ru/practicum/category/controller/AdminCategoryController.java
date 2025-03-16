package ru.practicum.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.dto.UpdateCategoryDto;
import ru.practicum.category.service.CategoryService;

/**
 * Контроллеры для управления категориями (для admin)
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService service;

    //создать
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody @Valid NewCategoryDto category) {
        return service.create(category);
    }

    //обновить
    @PatchMapping("/{catId}")
    public CategoryDto update(@RequestBody @Valid UpdateCategoryDto category,
                              @PathVariable("catId") Long catId) {
        return service.update(category, catId);
    }

    //удалить
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("catId") Long catId) {
        service.deleteById(catId);
    }
}
