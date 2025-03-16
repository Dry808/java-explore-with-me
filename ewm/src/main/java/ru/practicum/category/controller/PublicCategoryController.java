package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.List;

/**
 * Контроллер для категорий (публичный)
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {
    private final CategoryService service;

    @GetMapping
    public List<CategoryDto> getAll(@RequestParam(defaultValue = "0", required = false) Integer from,
                                    @RequestParam(defaultValue = "10", required = false) Integer size) {
        return service.getAll(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getById(@PathVariable("catId") Long catId) {
        return service.getById(catId);
    }
}
