package com.bcm.shared.service;

import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Configuration
@Profile({"!test"})
public class NodeStartupRegisterService {

    private static final Logger log = LoggerFactory.getLogger(NodeStartupRegisterService.class);

    private final RestTemplate restTemplate;
    private final Environment environment;

    // These values come from environment variables or application.properties
    @Value("${application.cm.public-address:localhost:8080}")
    private String cmPublicAddress;

    @Value("${application.node.public-address:localhost:8081}")
    private String nodePublicAddress;

    private boolean registered = false;

    public NodeStartupRegisterService(Environment environment) {
        this.restTemplate = new RestTemplate();
        this.environment = environment;
    }

    @Scheduled(cron = "*/10 * * * * *") // every 10 seconds
    public void registerAtStartup() {

        boolean profileSaysClusterManager = environment != null && Arrays.asList(environment.getActiveProfiles()).contains("cluster_manager");

        if (registered) {
            return; // skip once registration is done
        }

        NodeMode nodeType = profileSaysClusterManager ? NodeMode.CLUSTER_MANAGER : NodeMode.NODE;

        RegisterRequest req = new RegisterRequest(nodePublicAddress, nodeType);

        String cmRegisterUrl = "http://" + cmPublicAddress + "/api/v1/cm/register";

        log.info("Node starting with address: {}", nodePublicAddress);
        log.info("CM register endpoint: {}", cmRegisterUrl);

        try {
            restTemplate.postForEntity(cmRegisterUrl, req, Void.class);
            log.info("Successfully registered node with CM as {}.", nodeType);
            registered = true; // stop future attempts
        } catch (Exception e) {
            log.warn("Register attempt failed: {}", e.getMessage());
        }
    }
}