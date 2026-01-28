package com.bcm.shared.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.bcm.shared.util.NodeUtils;

import java.time.Duration;
import java.util.Objects;

@Component
public class NodeHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(NodeHttpClient.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;

    public NodeHttpClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public <T> Mono<T> callNode(String nodeAddress, String endpoint, Class<T> responseType) {
        Objects.requireNonNull(responseType, "Response type cannot be null");
        String url = NodeUtils.buildNodeUrl(nodeAddress, endpoint);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .doOnError(e -> logger.warn("Error calling {} on node {}: {}", endpoint, nodeAddress, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    public <T> T callNodeSync(String nodeAddress, String endpoint, Class<T> responseType) {
        return callNode(nodeAddress, endpoint, responseType).block();
    }

    public <T> Mono<T> postNode(String nodeAddress, String endpoint, Object body, Class<T> responseType) {
        String url = NodeUtils.buildNodeUrl(nodeAddress, endpoint);

        return webClient.post()
                .uri(url)
                .bodyValue(body != null ? body : "")
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .doOnError(e -> logger.warn("Error posting to {} on node {}: {}", endpoint, nodeAddress, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    public <T> T postNodeSync(String nodeAddress, String endpoint, Object body, Class<T> responseType) {
        return postNode(nodeAddress, endpoint, body, responseType).block();
    }

    public Mono<Boolean> postNodeNoResponse(String nodeAddress, String endpoint) {
        String url = NodeUtils.buildNodeUrl(nodeAddress, endpoint);

        return webClient.post()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .timeout(DEFAULT_TIMEOUT)
                .map(response -> true)
                .doOnError(e -> logger.warn("Error posting to {} on node {}: {}", endpoint, nodeAddress, e.getMessage()))
                .onErrorResume(e -> Mono.just(false));
    }

    public boolean postNodeSyncNoResponse(String nodeAddress, String endpoint) {
        return Boolean.TRUE.equals(postNodeNoResponse(nodeAddress, endpoint).block());
    }
}
