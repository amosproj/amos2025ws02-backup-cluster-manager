package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeMode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@SuppressWarnings("null")
class HeartbeatServiceTests {

    private HeartbeatService heartbeatService;

    private RegistryService registry = mock(RegistryService.class);
    private SyncService syncService = mock(SyncService.class);
    private WebClient webClient;
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setup() throws Exception {
        registry = mock(RegistryService.class);
        syncService = mock(SyncService.class);
        webClient = mock(WebClient.class);
        requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        WebClient.Builder builder = mock(WebClient.Builder.class);
        when(builder.build()).thenReturn(webClient);

        heartbeatService = new HeartbeatService(builder);

        Field regField = HeartbeatService.class.getDeclaredField("registry");
        regField.setAccessible(true);
        regField.set(heartbeatService, registry);

        Field syncField = HeartbeatService.class.getDeclaredField("syncService");
        syncField.setAccessible(true);
        syncField.set(heartbeatService, syncService);
    }

    @Test
    void heartbeatAll_callsPingForActiveAndInactive_andPushesTables() {
        NodeDTO a1 = new NodeDTO(1L, "A", "10.1.1.1:9000", com.bcm.shared.model.api.NodeStatus.ACTIVE, NodeMode.NODE, false, LocalDateTime.now());
        NodeDTO i1 = new NodeDTO(2L, "B", "10.1.1.2:9000", com.bcm.shared.model.api.NodeStatus.INACTIVE, NodeMode.NODE, false, LocalDateTime.now());

        when(registry.getActiveNodes()).thenReturn(List.of(a1));
        when(registry.getInactiveNodes()).thenReturn(List.of(i1));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(
                Mono.just(org.springframework.http.ResponseEntity.ok().build())
        );

        heartbeatService.heartbeatAll();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(webClient, times(2)).get();
        verify(syncService, timeout(200).times(1)).syncNodes();
    }

    @Test
    void pingNode_marksActiveOnSuccess() {
        NodeDTO a1 = new NodeDTO(1L, "A", "10.1.1.1:9000", com.bcm.shared.model.api.NodeStatus.ACTIVE, NodeMode.NODE, false, LocalDateTime.now());

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(mock(org.springframework.http.ResponseEntity.class)));

        StepVerifier.create(heartbeatService.pingNode(a1))
                .verifyComplete();

        verify(registry, times(1)).markActive(a1);
        verify(registry, never()).markInactive(any(NodeDTO.class));
    }


    @Test
    void pingNode_marksInactiveOnHttpError() {
        NodeDTO a1 = new NodeDTO(1L, "A", "10.1.1.1:9000", com.bcm.shared.model.api.NodeStatus.ACTIVE, NodeMode.NODE, false, LocalDateTime.now());

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.error(new WebClientResponseException(400, "Bad Request", null, null, null)));

        StepVerifier.create(heartbeatService.pingNode(a1))
                .verifyComplete();

        verify(registry).markInactive(a1);
        verify(registry, never()).markActive(any(NodeDTO.class));
    }


    @Test
    void pingNode_marksInactiveOnException() {
        NodeDTO a1 = new NodeDTO(1L, "A", "10.1.1.1:9000", com.bcm.shared.model.api.NodeStatus.ACTIVE, NodeMode.NODE, false, LocalDateTime.now());

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.error(new RuntimeException("Connection refused")));

        StepVerifier.create(heartbeatService.pingNode(a1))
                .verifyComplete();

        verify(registry).markInactive(a1);
        verify(registry, never()).markActive(any(NodeDTO.class));
    }

}