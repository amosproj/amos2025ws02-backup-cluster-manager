package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeMode;

import com.bcm.shared.model.api.SyncDTO;
import com.bcm.shared.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Disabled("Skipping Spring context startup for now")
class SyncServiceTests {

    private SyncService syncService;
    private RegistryService registryMock;
    private ExchangeFunction exchangeFunctionMock;
    private UserService userServiceMock;

    @BeforeEach
    void setup() throws Exception {
        userServiceMock = mock(UserService.class);
        registryMock = mock(RegistryService.class);
        exchangeFunctionMock = mock(ExchangeFunction.class);

        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(exchangeFunctionMock);

        syncService = new SyncService(userServiceMock, builder);

        // If registry is not constructor-injected, keep reflection for it
        var registryField = SyncService.class.getDeclaredField("registry");
        registryField.setAccessible(true);
        registryField.set(syncService, registryMock);
    }

    @Test
    void pushTablesToAllNodes_sendsSyncToAllActiveNodes() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "node1:8080",
                com.bcm.shared.model.api.NodeStatus.ACTIVE, NodeMode.NODE, true, LocalDateTime.now().minusDays(1));
        NodeDTO n2 = new NodeDTO(2L, "Node B", "node2:8080",
                com.bcm.shared.model.api.NodeStatus.ACTIVE, NodeMode.NODE, true, LocalDateTime.now().minusDays(2));

        when(userServiceMock.getAllUsers()).thenReturn(Flux.empty());

        when(registryMock.getActiveAndManagedNodes()).thenReturn(List.of(n1, n2));
        when(registryMock.getActiveNodes()).thenReturn(List.of(n1, n2));
        when(registryMock.getInactiveNodes()).thenReturn(List.of());

        when(exchangeFunctionMock.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(ClientResponse.create(HttpStatus.OK).build()));

        // Act
        syncService.syncNodes();

        // Assert
        ArgumentCaptor<ClientRequest> reqCaptor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(exchangeFunctionMock, times(2)).exchange(reqCaptor.capture());

        List<String> urls = reqCaptor.getAllValues().stream()
                .map(r -> r.url().toString())
                .toList();

        assertThat(urls).containsExactlyInAnyOrder(
                "http://node1:8080/api/v1/sync",
                "http://node2:8080/api/v1/sync"
        );
    }

    @Test
    void asyncPush_handlesFailureAndStillCompletes() {
        when(exchangeFunctionMock.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Connection error")));

        SyncDTO dto = new SyncDTO(List.of());

        when(exchangeFunctionMock.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Connection error")));

        syncService.syncNodes(); // or syncService.syncNodes().block() if reactive

        // verify it attempted a call, and didn’t crash
        verify(exchangeFunctionMock, atLeastOnce()).exchange(any(ClientRequest.class));

    }
}