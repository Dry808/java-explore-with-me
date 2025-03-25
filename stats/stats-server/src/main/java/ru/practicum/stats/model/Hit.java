package ru.practicum.stats.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Модель - информации о том, что на uri конкретного сервиса был отправлен запрос
 */
@Entity
@Data
@Builder
@Table(name = "hit")
public class Hit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String app;
    @Column(nullable = false)
    private String uri;
    @Column(nullable = false)
    private String ip;
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
