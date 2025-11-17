package com.bcm.cluster_manager;

import com.bcm.shared.model.api.ClusterTablesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
public class SyncService {
    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    @Autowired
    private RegistryService registry;

    private final RestTemplate rest = new RestTemplate();

    public void pushTablesToAllNodes() {
        ClusterTablesDTO tables = new ClusterTablesDTO(registry.getActiveNodes(), registry.getInactiveNodes());
        registry.getActiveNodes().forEach(node -> asyncPush(node.getAddress(), tables));
    }

    @Async
    protected CompletableFuture<Void> asyncPush(String address, ClusterTablesDTO dto) {
        String url = "http://" + address + "/api/v1/sync";
        try {
            rest.postForEntity(url, dto, Void.class);
            logger.info("Pushed tables to {}", address);
        } catch (Exception e) {
            logger.warn("Failed to push tables to {}: {}", address, e.toString());
        }
        return CompletableFuture.completedFuture(null);
    }
}