package ru.practicum.stats.storage;

/**
 * Для проекции данных
 */
public interface ViewStatsProjection {
    String getApp();

    String getUri();

    Long getHits();
}
