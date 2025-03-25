package ru.practicum.ewm.comment.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

/**
 * Модель описывающая комментарии
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "text", nullable = false)
    private String text;
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    @Column(name = "is_moderated", nullable = false)
    @Builder.Default
    private Boolean isModerated = false;
    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @PrePersist
    private void setCreatedOn() {
        this.createdOn = LocalDateTime.now();
    }
}
