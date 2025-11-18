package com.bcm.cluster_manager;

import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.pagination.PaginationResponse;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClusterManagerControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ClusterManagerService clusterManagerService;

    @Test
    void nodesEndpoint_returnsPaginatedList() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "Active", LocalDateTime.now().minusDays(1));
        NodeDTO n2 = new NodeDTO(2L, "Node B", "Inactive", LocalDateTime.now().minusDays(2));

        // Mock für die neue paginierte Methode
        when(clusterManagerService.getPaginatedItems(1, 2))
                .thenReturn(new PaginationResponse<>(List.of(n1, n2), 1, 1));

        ResponseEntity<PaginationResponse<NodeDTO>> resp =
                restTemplate.exchange(
                        "/api/v1/nodes?page=0&itemsPerPage=2",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<PaginationResponse<NodeDTO>>() {}
                );

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getItems()).hasSize(2);
    }
}