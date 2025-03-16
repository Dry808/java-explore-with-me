package ru.practicum.stats.model;

import jakarta.persistence.GeneratedValue;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Модель - информации о том, что на uri конкретного сервиса был отправлен запрос
 */
@Builder
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hits")
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "app", nullable = false)
    private String app;
    @Column(name = "uri", nullable = false)
    private String uri;
    @Column(name = "ip", nullable = false)
    private String ip;
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
