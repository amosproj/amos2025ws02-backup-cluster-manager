package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.ClusterTablesDTO;
import com.bcm.shared.model.api.NodeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SyncServiceTests {

    private SyncService syncService;
    private RegistryService registryMock;
    private RestTemplate restTemplateMock;

    @BeforeEach
    void setup() throws Exception {
        syncService = new SyncService();

        registryMock = mock(RegistryService.class);
        restTemplateMock = mock(RestTemplate.class);

        // Inject registry mock
        var registryField = SyncService.class.getDeclaredField("registry");
        registryField.setAccessible(true);
        registryField.set(syncService, registryMock);

        // Inject RestTemplate mock
        var restField = SyncService.class.getDeclaredField("rest");
        restField.setAccessible(true);
        restField.set(syncService, restTemplateMock);
    }

    @Test
    void pushTablesToAllNodes_sendsSyncToAllActiveNodes() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "node1:8080", "Active", LocalDateTime.now().minusDays(1));
        NodeDTO n2 = new NodeDTO(2L, "Node B", "node2:8080" , "Active", LocalDateTime.now().minusDays(2));

        when(registryMock.getActiveNodes()).thenReturn(List.of(n1, n2));
        when(registryMock.getInactiveNodes()).thenReturn(List.of());

        syncService.pushTablesToAllNodes();

        verify(restTemplateMock, times(2))
                .postForEntity(anyString(), any(ClusterTablesDTO.class), eq(Void.class));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplateMock, times(2))
                .postForEntity(urlCaptor.capture(), any(ClusterTablesDTO.class), eq(Void.class));

        List<String> urls = urlCaptor.getAllValues();
        assertThat(urls).containsExactlyInAnyOrder(
                "http://node1:8080/api/v1/sync",
                "http://node2:8080/api/v1/sync"
        );
    }

    @Test
    void asyncPush_handlesFailureAndStillCompletes() {
        when(restTemplateMock.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new RuntimeException("Connection error"));

        ClusterTablesDTO dto = new ClusterTablesDTO(List.of(), List.of());

        CompletableFuture<Void> result =
                syncService.asyncPush("badnode:8080", dto);

        verify(restTemplateMock, times(1))
                .postForEntity(eq("http://badnode:8080/api/v1/sync"), eq(dto), eq(Void.class));

        assertThat(result).isCompleted();
    }
}