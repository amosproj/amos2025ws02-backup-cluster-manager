package com.bcm.shared.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
public class NodeHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(NodeHttpClient.class);

    @Autowired
    private RestTemplate restTemplate;

    public String buildNodeUrl(String nodeAddress, String endpoint) {
        Objects.requireNonNull(nodeAddress, "Node address cannot be null");
        Objects.requireNonNull(endpoint, "Endpoint cannot be null");

        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }

        return "http://" + nodeAddress + endpoint;
    }

    public <T> CompletableFuture<T> callNodeAsync(String nodeAddress, String endpoint, Class<T> responseType) {
        Objects.requireNonNull(responseType, "Response type cannot be null");

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = buildNodeUrl(nodeAddress, endpoint);
                ResponseEntity<T> response = restTemplate.getForEntity(url, responseType);
                return response.getBody();
            } catch (Exception e) {
                logger.warn("Error calling {} on node {}: {}", endpoint, nodeAddress, e.getMessage());
                return null;
            }
        });
    }

    public <T> T callNodeSync(String nodeAddress, String endpoint, Class<T> responseType) {
        try {
            String url = buildNodeUrl(nodeAddress, endpoint);
            ResponseEntity<T> response = restTemplate.getForEntity(url, responseType);
            return response.getBody();
        } catch (Exception e) {
            logger.warn("Error calling {} on node {}: {}", endpoint, nodeAddress, e.getMessage());
            return null;
        }
    }
}
