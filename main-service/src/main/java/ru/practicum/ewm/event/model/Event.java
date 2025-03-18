package ru.practicum.ewm.event.model;

import jakarta.persistence.Entity;
import lombok.*;
import jakarta.persistence.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.location.Location;
import ru.practicum.ewm.user.User;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "events")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "annotation", nullable = false, length = 2000)
    String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "initiator_id")
    User initiator;

    @Column(name = "confirmed_requests")
    Integer confirmedRequests;

    @Column(name = "description", length = 7000)
    String description;

    @Column(name = "title", nullable = false, length = 120)
    String title;

    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @Column(name = "create_date")
    LocalDateTime createdDate;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "location_id")
    Location location;

    @Column(name = "paid")
    boolean paid;

    @Column(name = "participant_limit")
    Integer participantLimit;

    @Column(name = "published_date")
    LocalDateTime publisherDate;

    @Column(name = "request_moderation")
    boolean requestModeration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    EventStatus eventStatus;

}