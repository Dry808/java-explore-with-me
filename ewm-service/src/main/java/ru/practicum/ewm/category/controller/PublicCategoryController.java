package ru.practicum.ewm.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import java.util.List;

/**
 * Контроллер для взаимодействия с категориями (public)
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {
    private final CategoryService service;

    // получить все категории
    @GetMapping
    public List<CategoryDto> getAll(@RequestParam(defaultValue = "0", required = false) Integer from,
                                    @RequestParam(defaultValue = "10", required = false) Integer size) {
        return service.getAll(from, size);
    }

    // Получить категорию по ID
    @GetMapping("/{catId}")
    public CategoryDto getById(@PathVariable("catId") Long catId) {
        return service.getById(catId);
    }
}
