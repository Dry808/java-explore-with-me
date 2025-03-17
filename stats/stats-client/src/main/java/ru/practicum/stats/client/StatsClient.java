package ru.practicum.stats.client;


import org.springframework.http.ResponseEntity;

import java.util.List;

public interface StatsClient {
    void addHit(String uri, String ip);

    public ResponseEntity<Object> getStats(String start, String end, List<String> uris, Boolean unique);
}
