package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeMode;

import ch.qos.logback.core.pattern.parser.Node;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

class HeartbeatServiceTests {

    private HeartbeatService heartbeatService;

    private RegistryService registry = mock(RegistryService.class);
    private SyncService syncService = mock(SyncService.class);
    private RestTemplate restTemplate = mock(RestTemplate.class);

    @BeforeEach
    void setup() throws Exception {
        heartbeatService = new HeartbeatService();

        Field regField = HeartbeatService.class.getDeclaredField("registry");
        regField.setAccessible(true);
        regField.set(heartbeatService, registry);

        Field syncField = HeartbeatService.class.getDeclaredField("syncService");
        syncField.setAccessible(true);
        syncField.set(heartbeatService, syncService);

        Field restField = HeartbeatService.class.getDeclaredField("rest");
        restField.setAccessible(true);
        restField.set(heartbeatService, restTemplate);
    }

    @Test
    void heartbeatAll_callsPingForActiveAndInactive_andPushesTables() {
    NodeDTO a1 = new NodeDTO(1L, "A","10.1.1.1:9000", com.bcm.shared.model.api.NodeStatus.ACTIVE, NodeMode.BACKUP_NODE, LocalDateTime.now());
    NodeDTO i1 = new NodeDTO(2L, "B","10.1.1.2:9000", com.bcm.shared.model.api.NodeStatus.INACTIVE, NodeMode.BACKUP_NODE, LocalDateTime.now());

        when(registry.getActiveNodes()).thenReturn(List.of(a1));
        when(registry.getInactiveNodes()).thenReturn(List.of(i1));

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("pong", HttpStatus.OK));

        heartbeatService.heartbeatAll();

        verify(restTemplate, times(2)).getForEntity(contains("/api/v1/ping"), eq(String.class));

        verify(registry, times(2)).markActive(anyString(), any());

        verify(syncService, times(1)).pushTablesToAllNodes();
    }

    @Test
    void pingNodeAsync_marksActiveOnSuccess() {
        String addr = "10.1.1.1:9000";

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("pong", HttpStatus.OK));

        CompletableFuture<Void> f = heartbeatService.pingNodeAsync(addr, NodeMode.BACKUP_NODE);
        f.join();

        verify(registry, times(1)).markActive(addr, NodeMode.BACKUP_NODE);
        verify(registry, never()).markInactive(anyString(), any());
    }

    @Test
    void pingNodeAsync_marksInactiveOnHttpError() {
        String addr = "10.1.1.1:9000";

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("fail", HttpStatus.BAD_REQUEST));

        heartbeatService.pingNodeAsync(addr, NodeMode.BACKUP_NODE).join();

        verify(registry).markInactive(addr, NodeMode.BACKUP_NODE);
        verify(registry, never()).markActive(anyString(), any());
    }

    @Test
    void pingNodeAsync_marksInactiveOnException() {
        String addr = "10.1.1.1:9000";

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        heartbeatService.pingNodeAsync(addr, NodeMode.BACKUP_NODE).join();

        verify(registry).markInactive(addr, NodeMode.BACKUP_NODE);
        verify(registry, never()).markActive(anyString(), any());
    }
}