package com.bcm.shared.service;

import com.bcm.shared.model.api.CacheEventDTO;
import com.bcm.shared.model.api.CacheInvalidationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class CacheEventStore {

        private static final Logger logger = LoggerFactory.getLogger(CacheEventStore.class);

        // queue to store events
        private final Queue<CacheEventDTO> eventQueue = new ConcurrentLinkedQueue<>();

        // Auto-cleanup old events (older than 10 minutes)
        private static final Duration EVENT_RETENTION = Duration.ofMinutes(10);

        public void recordEvent(CacheInvalidationType type, Long entityId) {
            CacheEventDTO event = new CacheEventDTO();
            event.setId(System.currentTimeMillis());
            event.setType(type);
            event.setEntityId(entityId);
            event.setTimestamp(Instant.now());

            eventQueue.offer(event);
            logger.debug("Recorded event: {}", type);

            // Cleanup old events
            cleanupOldEvents();
        }

        public List<CacheEventDTO> getEventsSince(Instant since) {
            return eventQueue.stream()
                    .filter(event -> event.getTimestamp().isAfter(since))
                    .sorted(Comparator.comparing(CacheEventDTO::getTimestamp))
                    .toList();
        }

        public List<CacheEventDTO> getAllUnprocessedEvents() {
            return new ArrayList<>(eventQueue);
        }

        public void acknowledgeEvents(List<Long> eventIds) {
            eventQueue.removeIf(event -> eventIds.contains(event.getId()));
            logger.debug("Acknowledged and removed {} events", eventIds.size());
        }

        private void cleanupOldEvents() {
            Instant cutoff = Instant.now().minus(EVENT_RETENTION);
            eventQueue.removeIf(event -> event.getTimestamp().isBefore(cutoff));
        }
}
