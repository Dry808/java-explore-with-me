package ru.practicum.ewm.event.storage;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.exceptions.DataValidationException;
import ru.practicum.ewm.event.dto.AdminEventFilterDto;
import ru.practicum.ewm.event.dto.EventFilterDto;
import ru.practicum.ewm.event.enums.EventState;

import java.time.LocalDateTime;

/**
 * Класс для построения условий фильтрации
 */
@Component
public class EventFilterBuilder {
    private final QEvent event = QEvent.event;

    // Создает предикат для фильтрации событий на основе параметров из EventFilterDto
    public BooleanExpression buildPredicate(EventFilterDto dto) {
        BooleanExpression predicate = event.isNotNull()
                .and(event.state.eq(EventState.PUBLISHED)); //событие должно существовать и быть в состоянии PUBLISHED

        if (dto.getText() != null && !dto.getText().isBlank()) {
            predicate = predicate.and(
                    event.annotation.likeIgnoreCase("%" + dto.getText() + "%")
                            .or(event.description.likeIgnoreCase("%" + dto.getText() + "%"))
            );
        }

        // Фильр по категориям
        if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
            predicate = predicate.and(event.category.id.in(dto.getCategories()));
        }

        // Фильтр по оплате
        if (dto.getPaid() != null) {
            predicate = predicate.and(event.paid.eq(dto.getPaid()));
        }

        // Фильтр по дате события
        LocalDateTime now = LocalDateTime.now();
        if (dto.getRangeStart() == null && dto.getRangeEnd() == null) {
            // Если диапазон дат не указан, выбираем события, которые начнутся в будущем
            predicate = predicate.and(event.eventDate.after(now));
        } else if (dto.getRangeStart() != null && dto.getRangeEnd() != null) {
            if (dto.getRangeStart().isAfter(dto.getRangeEnd())) { // проверяем
                throw new DataValidationException("Start должен быть раньше end");
            }
            // Фильтруем события, которые попадают в указанный диапазон
            predicate = predicate.and(event.eventDate.between(dto.getRangeStart(), dto.getRangeEnd()));
        } else if (dto.getRangeStart() != null) {
            // Если указана только начальная дата, выбираем события после неё
            predicate = predicate.and(event.eventDate.after(dto.getRangeStart()));
        } else {
            // Если указана только конечная дата, выбираем события до неё
            predicate = predicate.and(event.eventDate.before(dto.getRangeEnd()));
        }

        // фильтр по доступности
        if (dto.getOnlyAvailable() != null && dto.getOnlyAvailable()) {
            predicate = predicate.and(event.participantLimit.eq(0)
                    .or(event.confirmedRequests.lt(event.participantLimit)));
        }

        return predicate;
    }

    // Создает предикат для фильтрации событий на основе параметров из AdminEventFilterDto
    public BooleanExpression buildPredicate(AdminEventFilterDto dto) {
        BooleanExpression predicate = event.isNotNull();

        // Фильр по пользователям
        if (dto.getUsers() != null && !dto.getUsers().isEmpty()) {
            predicate = predicate.and(event.initiator.id.in(dto.getUsers()));
        }

        // категории
        if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
            predicate = predicate.and(event.category.id.in(dto.getCategories()));
        }
        // статус
        if (dto.getStates() != null && !dto.getStates().isEmpty()) {
            predicate = predicate.and(event.state.in(dto.getStates()));
        }
        // по дате
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
