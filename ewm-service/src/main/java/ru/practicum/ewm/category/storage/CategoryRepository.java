package ru.practicum.ewm.category.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.category.model.Category;
import java.util.Optional;

/**
 * Репозиторий для вазимодействия с БД (категории)
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}
