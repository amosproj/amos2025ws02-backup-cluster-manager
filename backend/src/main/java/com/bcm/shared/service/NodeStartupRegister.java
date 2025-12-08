package com.bcm.shared.service;

import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.RegisterRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * Handles registration of both regular nodes and cluster manager to the CM registry.
 * Uses profile-based configuration to determine the node type:
 * - With 'cluster_manager' profile: registers as CLUSTER_MANAGER
 * - Without 'cluster_manager' profile: registers as NODE
 */
@Configuration
@Profile("!test")
public class NodeStartupRegister {

    private static final Logger log = LoggerFactory.getLogger(NodeStartupRegister.class);

    private final RestTemplate restTemplate;
    private final Environment environment;

    @Value("${application.cm.public-address:localhost:8080}")
    private String cmPublicAddress;

    @Value("${application.node.public-address:localhost:8081}")
    private String nodePublicAddress;

    @Value("${application.register.max-attempts:10}")
    private int maxAttempts = 10;

    @Value("${application.register.retry-delay-ms:3000}")
    private long retryDelayMs = 3000;

    @Value("${application.is-cluster-manager:false}")
    private boolean isClusterManager;

    public NodeStartupRegister() {
        this(new RestTemplate(), null);
    }

    public NodeStartupRegister(RestTemplate restTemplate) {
        this(restTemplate, null);
    }

    public NodeStartupRegister(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    @Bean
    public ApplicationRunner registerAtStartup() {
        return args -> {
            String cmRegisterUrl = "http://" + cmPublicAddress + "/api/v1/cm/register";

            log.info("Node starting with address: {}", nodePublicAddress);
            log.info("CM register endpoint: {}", cmRegisterUrl);

            boolean profileSaysClusterManager = environment != null && Arrays.asList(environment.getActiveProfiles()).contains("cluster_manager");

            // Determine node type based on active profiles or explicit property
            NodeMode nodeType = (profileSaysClusterManager || isClusterManager) ? NodeMode.CLUSTER_MANAGER : NodeMode.NODE;

            RegisterRequest req = new RegisterRequest(nodePublicAddress, nodeType);

            boolean registered = false;
            int attempts = 0;

            while (!registered && attempts < maxAttempts) {
                attempts++;
                try {
                    restTemplate.postForEntity(cmRegisterUrl, req, Void.class);
                    log.info("Successfully registered node with CM as {}.", nodeType);
                    registered = true;
                } catch (Exception e) {
                    log.warn("Register attempt {} failed: {}", attempts, e.getMessage());
                    if (attempts < maxAttempts) {
                        Thread.sleep(retryDelayMs);
                    }
                }
            }

            if (!registered) {
                log.error("Node could NOT register with CM after {} attempts!", attempts);
            }
        };
    }
}