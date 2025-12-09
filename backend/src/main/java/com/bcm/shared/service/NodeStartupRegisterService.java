package com.bcm.shared.service;

import com.bcm.shared.model.api.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile({"!test"})
public class NodeStartupRegisterService {

    private static final Logger log = LoggerFactory.getLogger(NodeStartupRegisterService.class);

    @Autowired
    private RestTemplate restTemplate;

    // These values come from environment variables or application.properties
    @Value("${application.cm.public-address:localhost:8080}")
    private String cmPublicAddress;

    @Value("${application.node.public-address:localhost:8081}")
    private String nodePublicAddress;

    private boolean registered = false;

    @Scheduled(cron = "*/10 * * * * *") // every 10 seconds
    public void registerAtStartup() {

        if (registered) {
            return; // skip once registration is done
        }

        String cmRegisterUrl = "http://" + cmPublicAddress + "/api/v1/cm/register";

        log.info("Node starting with address: {}", nodePublicAddress);
        log.info("CM register endpoint: {}", cmRegisterUrl);

        RegisterRequest req = new RegisterRequest(nodePublicAddress);


        try {
            restTemplate.postForEntity(cmRegisterUrl, req, Void.class);
            log.info("Successfully registered node with CM.");
            registered = true; // stop future attempts
        } catch (Exception e) {
            log.warn("Register attempt failed: {}", e.getMessage());
        }
    }
}