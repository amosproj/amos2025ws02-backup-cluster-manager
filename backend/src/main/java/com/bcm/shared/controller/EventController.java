package com.bcm.shared.controller;

import com.bcm.shared.model.api.CacheEventDTO;
import com.bcm.shared.service.CacheEventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bn/events")
public class EventController {
    @Autowired
    private CacheEventStore eventStore;

    @GetMapping("/cache-invalidations")
    public ResponseEntity<List<CacheEventDTO>> getAllEvents() {
        return ResponseEntity.ok(eventStore.getAllUnprocessedEvents());
    }

    @GetMapping("/cache-invalidations/since")
    public ResponseEntity<List<CacheEventDTO>> getEventsSince(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
        return ResponseEntity.ok(eventStore.getEventsSince(since));
    }

    @PostMapping("/cache-invalidations/acknowledge")
    public ResponseEntity<Void> acknowledgeEvents(@RequestBody List<Long> eventIds) {
        eventStore.acknowledgeEvents(eventIds);
        return ResponseEntity.ok().build();
    }

}
