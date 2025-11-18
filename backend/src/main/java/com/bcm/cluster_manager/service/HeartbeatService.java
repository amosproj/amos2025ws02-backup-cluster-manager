package com.bcm.cluster_manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Service
public class HeartbeatService {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatService.class);
    private final RestTemplate rest = new RestTemplate();


    @Autowired
    private RegistryService registry;

    @Autowired
    private SyncService syncService;

    public void heartbeatAll() {
        logger.info("Heartbeat started at {}", Instant.now());
        registry.getActiveNodes().forEach(node -> pingNodeAsync(node.getAddress()));
        // optionally ping inactive nodes to try revive them (if they respond successfully, entry is set active or added)
        registry.getInactiveNodes().forEach(node -> pingNodeAsync(node.getAddress()));
        // after heartbeats, push updated tables to all nodes
        syncService.pushTablesToAllNodes();
    }

    @Async
    public CompletableFuture<Void> pingNodeAsync(String address) {
        String url = "http://" + address + "/api/v1/ping";
        try {
            ResponseEntity<String> r = rest.getForEntity(url, String.class);
            if (r.getStatusCode().is2xxSuccessful()) {
                logger.info("Node {} is alive", address);
                registry.markActive(address);
            } else {
                logger.warn("Node {} returned non-2xx: {}", address, r.getStatusCodeValue());
                registry.markInactive(address);
            }
        } catch (Exception e) {
            logger.warn("Node {} is unreachable: {}", address, e.toString());
            registry.markInactive(address);
        }
        return CompletableFuture.completedFuture(null);
    }
}
