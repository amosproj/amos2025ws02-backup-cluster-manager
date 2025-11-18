package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.ClusterManagerService;
import com.bcm.shared.model.api.NodeDTO;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClusterManagerControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ClusterManagerService clusterManagerService;

    @Test
    void nodesEndpoint_returnsList() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "Active", null, LocalDateTime.now().minusDays(1));
        NodeDTO n2 = new NodeDTO(2L, "Node B", "Inactive", null, LocalDateTime.now().minusDays(2));
        when(clusterManagerService.findNodes(null, null, null, null)).thenReturn(List.of(n1, n2));

        ResponseEntity<NodeDTO[]> resp =
            restTemplate.getForEntity("/api/v1/nodes", NodeDTO[].class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().length).isEqualTo(2);
        verify(clusterManagerService, times(1)).findNodes(null, null, null, null);
    }

    @Test
    void nodesEndpoint_withActiveFilter_returnsFilteredList() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "Active", null, LocalDateTime.now().minusDays(1));
        when(clusterManagerService.findNodes(true, null, null, null)).thenReturn(List.of(n1));

        ResponseEntity<NodeDTO[]> resp =
            restTemplate.getForEntity("/api/v1/nodes?active=true", NodeDTO[].class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().length).isEqualTo(1);
        assertThat(resp.getBody()[0].getStatus()).isEqualTo("Active");
        verify(clusterManagerService, times(1)).findNodes(true, null, null, null);
    }

    @Test
    void nodesEndpoint_withActiveFalse_callsFindNodesWithFalse() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "Active", null, LocalDateTime.now().minusDays(1));
        NodeDTO n2 = new NodeDTO(2L, "Node B", "Inactive", null, LocalDateTime.now().minusDays(2));
        when(clusterManagerService.findNodes(false, null, null, null)).thenReturn(List.of(n1, n2));

        ResponseEntity<NodeDTO[]> resp =
            restTemplate.getForEntity("/api/v1/nodes?active=false", NodeDTO[].class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        verify(clusterManagerService, times(1)).findNodes(false, null, null, null);
    }

    @Test
    void nodesEndpoint_withSearch_returnsMatchingNodes() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "Active", null, LocalDateTime.now().minusDays(1));
        when(clusterManagerService.findNodes(null, "Node A", null, null)).thenReturn(List.of(n1));

        ResponseEntity<NodeDTO[]> resp =
            restTemplate.getForEntity("/api/v1/nodes?search=Node A", NodeDTO[].class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().length).isEqualTo(1);
        assertThat(resp.getBody()[0].getName()).isEqualTo("Node A");
        verify(clusterManagerService, times(1)).findNodes(null, "Node A", null, null);
    }

    @Test
    void nodesEndpoint_withActiveAndSearch_returnsCombinedFilter() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "Active", null, LocalDateTime.now().minusDays(1));
        when(clusterManagerService.findNodes(true, "Node A", null, null)).thenReturn(List.of(n1));

        ResponseEntity<NodeDTO[]> resp =
            restTemplate.getForEntity("/api/v1/nodes?active=true&search=Node A", NodeDTO[].class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().length).isEqualTo(1);
        assertThat(resp.getBody()[0].getName()).isEqualTo("Node A");
        assertThat(resp.getBody()[0].getStatus()).isEqualTo("Active");
        verify(clusterManagerService, times(1)).findNodes(true, "Node A", null, null);
    }

    @Test
    void nodesEndpoint_withSearchById_returnsNode() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "Active", null, LocalDateTime.now().minusDays(1));
        when(clusterManagerService.findNodes(null, "1", null, null)).thenReturn(List.of(n1));

        ResponseEntity<NodeDTO[]> resp =
            restTemplate.getForEntity("/api/v1/nodes?search=1", NodeDTO[].class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().length).isEqualTo(1);
        assertThat(resp.getBody()[0].getId()).isEqualTo(1L);
        verify(clusterManagerService, times(1)).findNodes(null, "1", null, null);
    }

    @Test
    void nodesEndpoint_noMatches_returnsEmptyList() {
        when(clusterManagerService.findNodes(true, "NonExistent", null, null)).thenReturn(List.of());

        ResponseEntity<NodeDTO[]> resp =
            restTemplate.getForEntity("/api/v1/nodes?active=true&search=NonExistent", NodeDTO[].class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().length).isEqualTo(0);
        verify(clusterManagerService, times(1)).findNodes(true, "NonExistent", null, null);
    }

    @Test
    void nodesEndpoint_withEmptySearch_treatsAsNull() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "Active", null, LocalDateTime.now().minusDays(1));
        NodeDTO n2 = new NodeDTO(2L, "Node B", "Inactive", null, LocalDateTime.now().minusDays(2));
        when(clusterManagerService.findNodes(null, "", null, null)).thenReturn(List.of(n1, n2));

        ResponseEntity<NodeDTO[]> resp =
            restTemplate.getForEntity("/api/v1/nodes?search=", NodeDTO[].class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        verify(clusterManagerService, times(1)).findNodes(null, "", null, null);
    }
}