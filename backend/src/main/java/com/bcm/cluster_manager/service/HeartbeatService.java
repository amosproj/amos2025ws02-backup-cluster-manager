package com.bcm.cluster_manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bcm.shared.model.api.NodeDTO;

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
        registry.getActiveNodes().forEach(node -> pingNodeAsync(node));
        // optionally ping inactive nodes to try revive them (if they respond
        // successfully, entry is set active or added)
        registry.getInactiveNodes().forEach(node -> pingNodeAsync(node));
        // after heartbeats, push updated tables to all nodes
        syncService.pushTablesToAllNodes();
    }

    @Async
    public CompletableFuture<Void> pingNodeAsync(NodeDTO node) {
        String url = "http://" + node.getAddress() + "/api/v1/ping";
        try {
            ResponseEntity<String> r = rest.getForEntity(url, String.class);
            if (r.getStatusCode().is2xxSuccessful()) {
                logger.info("Node {} is alive", node.getAddress());
                registry.markActive(node);
            } else {
                logger.warn("Node {} returned non-2xx: {}", node.getAddress(), r.getStatusCodeValue());
                registry.markInactive(node);
            }
        } catch (Exception e) {
            logger.warn("Node {} is unreachable: {}", node.getAddress(), e.toString());
            registry.markInactive(node);
        }
        return CompletableFuture.completedFuture(null);
    }
}
