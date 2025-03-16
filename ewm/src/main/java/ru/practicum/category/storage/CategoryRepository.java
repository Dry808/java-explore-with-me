package ru.practicum.category.storage;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.category.model.Category;

import java.util.Optional;

/**
 * Репозиторий для работы с category
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}
