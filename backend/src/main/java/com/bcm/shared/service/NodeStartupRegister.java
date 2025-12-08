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
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile({"backup_node & !test", "backup_manager & !test"})
public class NodeStartupRegister {

    private static final Logger log = LoggerFactory.getLogger(NodeStartupRegister.class);

    @Autowired
    private RestTemplate restTemplate;

    // These values come from environment variables or application.properties
    @Value("${application.cm.public-address:localhost:8080}")
    private String cmPublicAddress;

    @Value("${application.node.public-address:localhost:8081}")
    private String nodePublicAddress;


    @Bean
    public ApplicationRunner registerAtStartup() {
        return args -> {
            String cmRegisterUrl = "http://" + cmPublicAddress + "/api/v1/cm/register";

            log.info("Node starting with address: {}", nodePublicAddress);
            log.info("CM register endpoint: {}", cmRegisterUrl);

            RegisterRequest req = new RegisterRequest(nodePublicAddress);

            boolean registered = false;
            int attempts = 0;

            while (!registered && attempts < 10) {
                attempts++;
                try {
                    restTemplate.postForEntity(cmRegisterUrl, req, Void.class);
                    log.info("Successfully registered node with CM.");
                    registered = true;
                } catch (Exception e) {
                    log.warn("Register attempt {} failed: {}", attempts, e.getMessage());
                    Thread.sleep(3000);
                }
            }

            if (!registered) {
                log.error("Node could NOT register with CM after {} attempts!", attempts);
            }
        };
    }
}