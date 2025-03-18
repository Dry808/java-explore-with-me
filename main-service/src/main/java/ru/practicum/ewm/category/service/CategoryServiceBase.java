package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryMapper;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.category.dto.CategoryInDto;
import ru.practicum.ewm.category.dto.CategoryOutDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidatetionConflict;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceBase implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryOutDto getById(Long id) {
        CategoryOutDto  categoryOutDto;
        categoryOutDto = CategoryMapper.toCategoryOutDto(findCategory(id));
        return categoryOutDto;
    }

    @Override
    public List<CategoryOutDto> get(Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);

        return categoryRepository.findAll(pageRequest)
                .stream()
                .map(CategoryMapper::toCategoryOutDto)
                .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public CategoryOutDto create(CategoryInDto categoryInDto) {
        return CategoryMapper.toCategoryOutDto(categoryRepository.save(CategoryMapper.toCategory(categoryInDto)));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Категория с id= " + id + " не найден");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CategoryOutDto update(Long id, CategoryInDto categoryInDto) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Категория с id= " + id + " не найдена");
        }
        Category category = findCategory(id);

        if (categoryInDto.getName() != null && !category.getName().equals(categoryInDto.getName()) && checkName(categoryInDto.getName())) {

            category.setName(categoryInDto.getName());
            category = categoryRepository.save(category);

        }
        return CategoryMapper.toCategoryOutDto(category);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Категории с id = " + id + " не существует"));
    }

    private boolean checkName(String name) {

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ValidatetionConflict("Категория с названием " + name + " уже существует");
        }
        return true;
    }
}