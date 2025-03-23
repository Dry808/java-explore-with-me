package ru.practicum.ewm.location.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель для описание локации
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Location {
    private Float lat;
    private Float lon;
}
