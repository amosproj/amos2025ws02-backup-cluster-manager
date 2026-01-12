package com.bcm.cluster_manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcm.shared.model.api.NodeDTO;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

@Service
public class HeartbeatService {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatService.class);


    @Autowired
    private RegistryService registry;

    @Autowired
    private SyncService syncService;

    private final WebClient webClient;

    public HeartbeatService(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.build();
    }

    public void heartbeatAll() {
        logger.info("Heartbeat started at {}", Instant.now());

        Collection<NodeDTO> activeNodes = registry.getActiveNodes();
        Collection<NodeDTO> inactiveNodes = registry.getInactiveNodes();

        // Ping all active and inactive nodes in parallel
        Flux.concat(
                Flux.fromIterable(activeNodes).flatMap(this::pingNode),
                Flux.fromIterable(inactiveNodes).flatMap(this::pingNode)
        )
        .doOnComplete(() -> {
            logger.info("Heartbeat completed, syncing nodes");
            syncService.syncNodes();
        })
        .subscribe();
    }

    public Mono<Void> pingNode(NodeDTO node) {
        String url = "http://" + node.getAddress() + "/api/v1/ping";

        return webClient.get()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(response -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        logger.info("Node {} is alive", node.getAddress());
                        registry.markActive(node);
                    } else {
                        logger.warn("Node {} returned non-2xx: {}", node.getAddress(), response.getStatusCode().value());
                        registry.markInactive(node);
                    }
                })
                .onErrorResume(e -> {
                    logger.warn("Node {} is unreachable: {}", node.getAddress(), e.toString());
                    registry.markInactive(node);
                    return Mono.empty();
                })
                .then();
    }
}
