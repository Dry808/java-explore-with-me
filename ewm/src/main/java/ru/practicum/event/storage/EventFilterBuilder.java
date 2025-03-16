package ru.practicum.event.storage;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.stereotype.Component;
import ru.practicum.event.dto.AdminEventFilterDto;
import ru.practicum.event.dto.EventFilterDto;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.QEvent;
import ru.practicum.exceptions.DataValidationException;
import java.time.LocalDateTime;

/**
 * Класс для построения условий фильтраций запросов к БД (Event)
 */
@Component
public class EventFilterBuilder {
    private final QEvent event = QEvent.event;

    // метод для создания предиката
    public BooleanExpression buildPredicate(EventFilterDto dto) {
        BooleanExpression predicate = event.isNotNull()
                .and(event.state.eq(EventState.PUBLISHED));

        // Фильтр по тексту (поиск в описании и в аннотации)
        if (dto.getText() != null && !dto.getText().isBlank()) {
            predicate = predicate.and(
                    event.annotation.likeIgnoreCase("%" + dto.getText() + "%")
                            .or(event.description.likeIgnoreCase("%" + dto.getText() + "%"))
            );
        }

        // Фильтр по категории
        if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
            predicate = predicate.and(event.category.id.in(dto.getCategories()));
        }

        // Фильтр бесплатный/платный
        if (dto.getPaid() != null) {
            predicate = predicate.and(event.paid.eq(dto.getPaid()));
        }

        // Фильтр по дате
        LocalDateTime now = LocalDateTime.now();
        if (dto.getRangeStart() == null && dto.getRangeEnd() == null) {
            predicate = predicate.and(event.eventDate.after(now)); // если нет дат, ищем события из будущего
        } else if (dto.getRangeStart() != null && dto.getRangeEnd() != null) {
            if (dto.getRangeStart().isAfter(dto.getRangeEnd())) { // проверка
                throw new DataValidationException("Start должен быть раньше end");
            }
            predicate = predicate.and(event.eventDate.between(dto.getRangeStart(), dto.getRangeEnd()));
        } else if (dto.getRangeStart() != null) { // Если указана только начальная дата, выбираем события после неё
            predicate = predicate.and(event.eventDate.after(dto.getRangeStart()));
        } else { // Если указана только конечная дата, выбираем события до неё
            predicate = predicate.and(event.eventDate.before(dto.getRangeEnd()));
        }

        // Фильтр по доступности (есть доступные места)
        if (dto.getOnlyAvailable() != null && dto.getOnlyAvailable()) {
            predicate = predicate.and(event.participantLimit.eq(0)
                    .or(event.confirmedRequests.lt(event.participantLimit)));
        }

        return predicate;
    }

    // метод для создания предиката для фильтрации (для админов)
    public BooleanExpression buildPredicate(AdminEventFilterDto dto) {
        BooleanExpression predicate = event.isNotNull();

        // Фильтр по пользователям
        if (dto.getUsers() != null && !dto.getUsers().isEmpty()) {
            predicate = predicate.and(event.initiator.id.in(dto.getUsers()));
        }

        // Фильтр по категориям
        if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
            predicate = predicate.and(event.category.id.in(dto.getCategories()));
        }

        // Фильтр по статусу
        if (dto.getStates() != null && !dto.getStates().isEmpty()) {
            predicate = predicate.and(event.state.in(dto.getStates()));
        }

        // Фильтр по дате
        if (dto.getRangeStart() != null && dto.getRangeEnd() != null) {
            predicate = predicate.and(event.eventDate.between(dto.getRangeStart(), dto.getRangeEnd()));
        } else if (dto.getRangeStart() != null) {
            predicate = predicate.and(event.eventDate.after(dto.getRangeStart()));
        } else if (dto.getRangeEnd() != null) {
            predicate = predicate.and(event.eventDate.before(dto.getRangeEnd()));
        }

        return predicate;
    }
}
