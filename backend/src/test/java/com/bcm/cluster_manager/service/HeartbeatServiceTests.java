package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.NodeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeartbeatServiceTests {

    @Mock
    private RegistryService registry;

    @Mock
    private SyncService syncService;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private HeartbeatService heartbeatService;

    @BeforeEach
    void setup() throws Exception {
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
        NodeDTO a1 = new NodeDTO(1L, "A", "10.1.1.1:9000", NodeStatus.ACTIVE, NodeMode.NODE, false, LocalDateTime.now());
        NodeDTO i1 = new NodeDTO(2L, "B", "10.1.1.2:9000", NodeStatus.INACTIVE, NodeMode.NODE, false, LocalDateTime.now());

        when(registry.getActiveNodes()).thenReturn(List.of(a1));
        when(registry.getInactiveNodes()).thenReturn(List.of(i1));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(
                Mono.just(ResponseEntity.ok().build())
        );

        heartbeatService.heartbeatAll();

        verify(webClient, timeout(200).times(2)).get();
        verify(syncService, timeout(200).times(1)).syncNodes();
    }

    @Test
    void pingNode_marksActiveOnSuccess() {
        NodeDTO a1 = new NodeDTO(1L, "A", "10.1.1.1:9000", NodeStatus.ACTIVE, NodeMode.NODE, false, LocalDateTime.now());

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.ok().build()));

        StepVerifier.create(heartbeatService.pingNode(a1))
                .verifyComplete();

        verify(registry, times(1)).markActive(a1);
        verify(registry, never()).markInactive(any(NodeDTO.class));
    }


    @Test
    void pingNode_marksInactiveOnHttpError() {
        NodeDTO a1 = new NodeDTO(1L, "A", "10.1.1.1:9000", NodeStatus.ACTIVE, NodeMode.NODE, false, LocalDateTime.now());

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
        NodeDTO a1 = new NodeDTO(1L, "A", "10.1.1.1:9000", NodeStatus.ACTIVE, NodeMode.NODE, false, LocalDateTime.now());

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
