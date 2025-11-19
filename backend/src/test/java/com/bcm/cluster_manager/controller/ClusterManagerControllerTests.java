package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.BackupService;
import com.bcm.cluster_manager.service.ClusterManagerService;
import com.bcm.cluster_manager.service.RegistryService;
import com.bcm.cluster_manager.service.SyncService;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.RegisterRequest;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClusterManagerControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ClusterManagerService clusterManagerService;

    @MockBean
    private BackupService backupService;

    @MockBean
    private RegistryService registryService;

    @MockBean
    private SyncService syncService;

    @Test
    void nodesEndpoint_returnsList() {
        NodeDTO n1 = new NodeDTO(1L, "Node A","10.100.179.80:9300","Active", LocalDateTime.now().minusDays(1));
        NodeDTO n2 = new NodeDTO(2L, "Node B", "10.100.179.81:9333","Inactive", LocalDateTime.now().minusDays(2));
        when(clusterManagerService.getAllNodes()).thenReturn(List.of(n1, n2));

        ResponseEntity<NodeDTO[]> resp =
            restTemplate.getForEntity("/api/v1/nodes", NodeDTO[].class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().length).isEqualTo(2);
    }

    @Test
    void registerEndpoint_callsRegistryAndSyncService() {
        String nodeAddress = "10.100.179.90:9400";
        RegisterRequest request = new RegisterRequest(nodeAddress);
        HttpEntity<RegisterRequest> requestEntity = new HttpEntity<>(request);

        ResponseEntity<Void> response = restTemplate.postForEntity("/api/v1/register", requestEntity, Void.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(registryService, times(1)).register(nodeAddress);
        verify(syncService, times(1)).pushTablesToAllNodes();
    }
}