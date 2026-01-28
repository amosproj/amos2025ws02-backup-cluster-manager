package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.CacheEventDTO;
import com.bcm.shared.model.api.CacheInvalidationType;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.util.NodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class EventPollingService {

    private static final Logger logger = LoggerFactory.getLogger(EventPollingService.class);

    @Autowired
    private RegistryService registryService;

    @Autowired
    private CacheManager cacheManager;

    private final WebClient webClient;

    private final Map<String, Instant> lastPollTime = new ConcurrentHashMap<>();

    public EventPollingService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Scheduled(fixedRate = 5000)
    public void pollNodesForEvents() {
        Collection<NodeDTO> activeNodes = registryService.getActiveAndManagedNodes();

        activeNodes.forEach(node -> {
            String nodeAddress = node.getAddress();
            Instant since = lastPollTime.getOrDefault(nodeAddress,
                    Instant.now().minus(10, ChronoUnit.MINUTES));

            fetchAndProcessEvents(nodeAddress, since);
        });
    }

    private void fetchAndProcessEvents(String nodeAddress, Instant since) {
        String url = NodeUtils.buildNodeUrl(nodeAddress,
                "/api/v1/bn/events/cache-invalidations/since?since=" + since.toString());

        webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(CacheEventDTO.class)
                .collectList()
                .subscribe(
                        events -> {
                            if (!events.isEmpty()) {
                                processEvents(events);
                                acknowledgeEvents(nodeAddress, events);
                                lastPollTime.put(nodeAddress, Instant.now());
                            }
                        },
                        error -> logger.warn("Failed to poll events from {}: {}", nodeAddress, error.getMessage())
                );
    }

     void processEvents(List<CacheEventDTO> events) {
        Set<CacheInvalidationType> types = events.stream()
                .map(CacheEventDTO::getType)
                .collect(Collectors.toSet());

        logger.info("Processing {} events from node", events.size());

        if (types.stream().anyMatch(t -> t.name().startsWith("BACKUP_"))) {
            invalidateCache("backupPages");
        }
        if (types.stream().anyMatch(t -> t.name().startsWith("CLIENT_"))) {
            invalidateCache("clientPages");
        }
        if (types.stream().anyMatch(t -> t.name().startsWith("TASK_"))) {
            invalidateCache("taskPages");
        }
    }

    private void acknowledgeEvents(String nodeAddress, List<CacheEventDTO> events) {
        List<Long> eventIds = events.stream()
                .map(CacheEventDTO::getId)
                .toList();

        String url = NodeUtils.buildNodeUrl(nodeAddress,
                "/api/v1/bn/events/cache-invalidations/acknowledge");

        webClient.post()
                .uri(url)
                .bodyValue(eventIds)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        success -> logger.debug("Acknowledged {} events from {}", eventIds.size(), nodeAddress),
                        error -> logger.error("Failed to acknowledge events from {}", nodeAddress, error)
                );
    }

    private void invalidateCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            logger.info("Cache '{}' invalidated", cacheName);
        }
    }
}