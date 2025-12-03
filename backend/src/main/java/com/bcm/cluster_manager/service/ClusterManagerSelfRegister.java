package com.bcm.cluster_manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Registers the Cluster Manager itself as a node in the registry on startup.
 * This allows the CM to be visible in the node list and participate as a node in the cluster.
 */
@Configuration
@Profile({"cluster_manager & !test"})
public class ClusterManagerSelfRegister {

    private static final Logger log = LoggerFactory.getLogger(ClusterManagerSelfRegister.class);

    @Autowired
    private RegistryService registryService;

    @Value("${application.node.public-address:localhost:8080}")
    private String nodePublicAddress;

    @Bean
    public ApplicationRunner registerClusterManagerAsNode() {
        return args -> {
            log.info("Registering Cluster Manager itself as a node with address: {}", nodePublicAddress);
            
            try {
                // Register the cluster manager as a node in its own registry
                registryService.register(nodePublicAddress);
                log.info("Successfully registered Cluster Manager as a node in the registry.");
            } catch (Exception e) {
                log.error("Failed to register Cluster Manager as a node: {}", e.getMessage(), e);
            }
        };
    }
}
