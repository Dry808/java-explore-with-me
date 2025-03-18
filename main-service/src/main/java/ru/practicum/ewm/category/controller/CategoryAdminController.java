package ru.practicum.ewm.category.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryInDto;
import ru.practicum.ewm.category.dto.CategoryOutDto;
import ru.practicum.ewm.category.service.CategoryService;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
public class  CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryOutDto createCategory(@RequestBody @Valid CategoryInDto categoryInDto) {
        log.info("==> Cоздание новой категории: {}", categoryInDto);
        return categoryService.create(categoryInDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable(value = "catId") Long id) {
        log.info("==> Удалении категории с id =  {} ", id);
        categoryService.delete(id);
    }

    @PatchMapping("/{catId}")
    public CategoryOutDto updateCategory(@PathVariable(value = "catId") @Min(1) Long id,
                                         @RequestBody @Valid CategoryInDto categoryInDto) {
        log.info("==> Обновдение категории с id = {}", id);
        return categoryService.update(id, categoryInDto);
    }

}