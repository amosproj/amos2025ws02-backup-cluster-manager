package com.bcm.shared.controller;

import com.bcm.shared.model.api.CacheEventDTO;
import com.bcm.shared.service.CacheEventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for cache invalidation events: list and acknowledge events.
 */
@RestController
@RequestMapping("/api/v1/bn/events")
public class EventController {
    @Autowired
    private CacheEventStore eventStore;

    /**
     * Returns all unprocessed cache invalidation events.
     *
     * @return list of cache event DTOs
     */
    @GetMapping("/cache-invalidations")
    public ResponseEntity<List<CacheEventDTO>> getAllEvents() {
        return ResponseEntity.ok(eventStore.getAllUnprocessedEvents());
    }

    /**
     * Returns cache invalidation events since the given instant.
     *
     * @param since start time (ISO date-time)
     * @return list of cache event DTOs
     */
    @GetMapping("/cache-invalidations/since")
    public ResponseEntity<List<CacheEventDTO>> getEventsSince(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
        return ResponseEntity.ok(eventStore.getEventsSince(since));
    }

    /**
     * Acknowledges the given event ids (marks as processed).
     *
     * @param eventIds list of event ids to acknowledge
     * @return 200 on success
     */
    @PostMapping("/cache-invalidations/acknowledge")
    public ResponseEntity<Void> acknowledgeEvents(@RequestBody List<Long> eventIds) {
        eventStore.acknowledgeEvents(eventIds);
        return ResponseEntity.ok().build();
    }

}
