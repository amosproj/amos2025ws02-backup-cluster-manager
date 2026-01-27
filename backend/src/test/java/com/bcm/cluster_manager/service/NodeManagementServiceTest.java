package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.JoinDTO;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.NodeStatus;
import com.bcm.shared.model.api.RegisterRequest;
import com.bcm.shared.service.NodeIdGenerator;
import com.bcm.shared.service.NodeHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NodeManagementServiceTest {

    @Mock
    private RegistryService registry;

    @Mock
    private SyncService syncService;

    @Mock
    private NodeHttpClient nodeHttpClient;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private NodeManagementService service;

    @BeforeEach
    void setUp() {
        WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);
        when(webClientBuilder.build()).thenReturn(webClient);

        service = new NodeManagementService(webClientBuilder);

        ReflectionTestUtils.setField(service, "registry", registry);
        ReflectionTestUtils.setField(service, "syncService", syncService);
        ReflectionTestUtils.setField(service, "nodeHttpClient", nodeHttpClient);
    }

    @Test
    void deleteNodeAndReRegister_shouldAllowReRegistrationAfterNotification() {
        // 1. node is added to the cluster
        // 2. node is deleted from the cluster
        // 3. node is added again


        Long nodeId = NodeIdGenerator.nextId();
        NodeDTO node = new NodeDTO(nodeId, "TestNode", "", NodeStatus.ACTIVE, NodeMode.NODE, false, null);
        RegisterRequest registerRequest = new RegisterRequest("", NodeMode.NODE, false);

        // this is supposed to be final, so it is an array to be able to set the value anyway
        final boolean[] hasJoined = {false};
        final List<String> calledUris = new ArrayList<>();

        // Mock registry
        when(registry.findById(nodeId)).thenReturn(Optional.of(node));
        doNothing().when(registry).removeNode(nodeId);
        doNothing().when(registry).register(registerRequest);
        when(syncService.syncNodes()).thenReturn(Mono.empty());

        // Simulate node JoinController
        when(webClient.post()).thenReturn(requestBodyUriSpec);

        // Capture URIs called
        when(requestBodyUriSpec.uri(anyString())).thenAnswer(invocation -> {
            String uri = invocation.getArgument(0);
            calledUris.add(uri);
            return requestBodySpec;
        });

        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any(JoinDTO.class));
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // Simulate JoinController responses based on join state
        when(responseSpec.toBodilessEntity()).thenAnswer(invocation -> {
            String lastUri = calledUris.isEmpty() ? "" : calledUris.getLast();

            if (lastUri.contains("/api/v1/bn/join")) {
                if (hasJoined[0]) {
                    return Mono.error(WebClientResponseException.create(
                            HttpStatus.CONFLICT.value(),
                            "Already joined",
                            null, null, null));
                } else {
                    hasJoined[0] = true;
                    return Mono.just(ResponseEntity.ok().build());
                }
            } else if (lastUri.contains("/api/v1/bn/leave")) {
                if (!hasJoined[0]) {
                    return Mono.error(WebClientResponseException.create(
                            HttpStatus.NOT_FOUND.value(),
                            "No cluster",
                            null, null, null));
                } else {
                    hasJoined[0] = false;
                    return Mono.just(ResponseEntity.ok().build());
                }
            }
            return Mono.just(ResponseEntity.ok().build());
        });

        // Add node
        StepVerifier.create(service.registerNode(registerRequest)).verifyComplete();

        // Verify first add
        verify(registry, times(1)).register(registerRequest);
        assertThat(hasJoined[0]).isTrue();

        // Delete node
        StepVerifier.create(service.deleteNode(nodeId)).verifyComplete();

        // Verify delete
        verify(registry, times(1)).removeNode(nodeId);

        // Add node again
        StepVerifier.create(service.registerNode(registerRequest)).verifyComplete();

        // Verify second registration succeeded
        verify(registry, times(2)).register(registerRequest);
    }

    @Test
    void shutdownAndRestart_shouldHandleGuardsAndFailures() {

        NodeDTO cm   = new NodeDTO(10L, "cm",   "cm:8080",   NodeStatus.ACTIVE, NodeMode.CLUSTER_MANAGER, false, null);
        NodeDTO node = new NodeDTO(11L, "node", "n1:8080",   NodeStatus.ACTIVE, NodeMode.NODE,            false, null);

        when(registry.findById(10L)).thenReturn(Optional.of(cm));
        when(registry.findById(11L)).thenReturn(Optional.of(node));

        // shutdown should reject CM and not call client
        StepVerifier.create(service.shutdownNode(10L))
                .expectNext(false)
                .verifyComplete();
        verifyNoInteractions(nodeHttpClient);

        // shutdown success should mark shutting down, mark inactive, and sync
        when(nodeHttpClient.postNodeNoResponse("n1:8080", "/api/v1/shutdown")).thenReturn(Mono.just(true));
        when(syncService.syncNodes()).thenReturn(Mono.empty());

        StepVerifier.create(service.shutdownNode(11L))
                .expectNext(true)
                .verifyComplete();

        verify(registry).markShuttingDown("n1:8080");
        verify(registry).markInactive(node);
        verify(syncService).syncNodes();

        // restart failure should mark restarting + mark inactive, but no sync call in this method
        when(nodeHttpClient.postNodeNoResponse("n1:8080", "/api/v1/restart")).thenReturn(Mono.just(false));

        StepVerifier.create(service.restartNode(11L))
                .expectNext(false)
                .verifyComplete();

        verify(registry).markRestarting("n1:8080");
        verify(registry, times(2)).markInactive(node);

        service.updateNodeManagedMode(node);
        verify(registry).updateIsManaged(node);

        when(registry.findByAddress("n1:8080")).thenReturn(Optional.of(node));
        assertThat(service.getNodeByAddress("n1:8080")).contains(node);
    }
}
