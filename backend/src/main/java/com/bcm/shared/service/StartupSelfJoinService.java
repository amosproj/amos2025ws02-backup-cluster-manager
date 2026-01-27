package com.bcm.shared.service;

import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;

import static com.bcm.shared.controller.JoinController.hasJoined;


@Component
@ConditionalOnProperty(
        name = "application.feature.self-registration",
        havingValue = "true",
        matchIfMissing = false
)
public class StartupSelfJoinService {

    private static final Logger log = LoggerFactory.getLogger(StartupSelfJoinService.class);

    private final Environment environment;

    @Value("${application.cm.public-address:localhost:8080}")
    private String cmPublicAddress;

    @Value("${application.node.public-address:localhost:8081}")
    private String nodePublicAddress;

    private final WebClient webClient;

    public StartupSelfJoinService(WebClient.Builder webClientBuilder, Environment environment) {
        this.webClient = webClientBuilder.build();
        this.environment = environment;
    }

    public boolean isClusterManagerActive() {
        return Arrays.asList(environment.getActiveProfiles())
                .contains("cluster_manager");
    }

    @Scheduled(cron = "*/1 * * * * *") // every minute (adjust as needed)
    public void registerSelfCron() {
        registerSelf()
                .subscribe(
                        null,
                        ex -> log.error("Self-registration failed after retries")
                );
    }

    private Mono<Void> registerSelf() {
        if(hasJoined){
            return Mono.empty();
        }
        NodeMode nodeType = isClusterManagerActive() ? NodeMode.CLUSTER_MANAGER : NodeMode.NODE;
        RegisterRequest registerRequest = new RegisterRequest(nodePublicAddress, nodeType, true);

        String url = "http://" + cmPublicAddress + "/api/v1/cm/register";

        log.info("Initiating self-registration for {}...", nodeType);

        return webClient.post()
                .uri(url)
                .bodyValue(registerRequest)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(resp ->
                        log.info("Sent request for self-register"))
                .doOnError(e ->
                        log.error("Error registering node", e))
                .then();

    }


}