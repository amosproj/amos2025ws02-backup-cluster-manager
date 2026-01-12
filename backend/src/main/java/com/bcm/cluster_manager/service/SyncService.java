package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.SyncDTO;
import com.bcm.shared.model.database.User;
import com.bcm.shared.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class SyncService {
    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    @Autowired
    private RegistryService registry;

    private final UserService userService;
    private final WebClient webClient;

    public SyncService(@Qualifier("userServiceCM") UserService userService, WebClient.Builder webClientBuilder) {
        this.userService = userService;
        this.webClient = webClientBuilder.build();
    }

    public void syncNodes() {
        List<User> cmUsers = userService.getAllUsers();
        SyncDTO dto = new SyncDTO();
        dto.setCmUsers(cmUsers);

        Flux.fromIterable(registry.getActiveAndManagedNodes())
                .flatMap(node -> pushToNode(node.getAddress(), dto))
                .subscribe();
    }

    private Mono<Void> pushToNode(String address, SyncDTO dto) {
        String url = "http://" + address + "/api/v1/sync";

        return webClient.post()
                .uri(url)
                .bodyValue(dto)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(response -> logger.info("Pushed users to {}", address))
                .onErrorResume(e -> {
                    logger.warn("Failed to push users to {}: {}", address, e.toString());
                    return Mono.empty();
                })
                .then();
    }
}