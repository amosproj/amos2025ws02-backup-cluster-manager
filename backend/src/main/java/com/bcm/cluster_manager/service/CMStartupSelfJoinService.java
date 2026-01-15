package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.util.retry.Retry;

import java.time.Duration;


@Component
@Profile("cluster_manager")
public class CMStartupSelfJoinService implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(CMStartupSelfJoinService.class);

    private final NodeManagementService nodeManagementService;

    @Value("${application.cm.public-address:localhost:8080}")
    private String cmPublicAddress;

    @Value("${application.node.public-address:localhost:8081}")
    private String nodePublicAddress;

    public CMStartupSelfJoinService(NodeManagementService nodeManagementService) {
        this.nodeManagementService = nodeManagementService;
    }

    @Override
    public void run(ApplicationArguments args) {
        registerSelf();
    }

    private void registerSelf() {
        NodeMode nodeType = NodeMode.CLUSTER_MANAGER;
        RegisterRequest registerRequest = new RegisterRequest(nodePublicAddress, nodeType);

        log.info("Initiating self-registration for {}...", nodeType);

        // Logic flow: Attempt registration -> if fail, retry -> on success, log and stop.
        nodeManagementService.registerNode(registerRequest)
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                        .doBeforeRetry(retrySignal ->
                                log.warn("Registration failed: {}. Retrying in 5s...",
                                        retrySignal.failure().getMessage()))
                )
                .subscribe(
                        null, // onSuccess - result is likely Void
                        error -> log.error("Fatal error during registration", error),
                        () -> log.info("SUCCESS: Registered self as {}", nodeType)
                );
    }


}