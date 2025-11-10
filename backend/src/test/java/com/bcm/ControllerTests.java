package com.bcm;

import api.model.BackupClass;
import api.model.NodeClass;

import com.bcm.backups.BackupService;
import com.bcm.nodes.NodeService;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private NodeService nodeService;

    @MockBean
    private BackupService backupService;

    @Test
    void nodesEndpoint_returnsList() {
        NodeClass n1 = new NodeClass(1L, "Node A", "Active", LocalDateTime.now().minusDays(1));
        NodeClass n2 = new NodeClass(2L, "Node B", "Inactive", LocalDateTime.now().minusDays(2));
        when(nodeService.getAllNodes()).thenReturn(List.of(n1, n2));

        ResponseEntity<NodeClass[]> resp =
            restTemplate.getForEntity("/api/v1/nodes", NodeClass[].class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().length).isEqualTo(2);
    }

    @Test
    void backupsEndpoint_returnsList() {
        BackupClass n1 = new BackupClass(1L, "Backup A", "Active", LocalDateTime.now().minusDays(1));
        BackupClass n2 = new BackupClass(2L, "Backup B", "Inactive", LocalDateTime.now().minusDays(2));
        when(backupService.getAllBackups()).thenReturn(List.of(n1, n2));

        ResponseEntity<BackupClass[]> resp =
            restTemplate.getForEntity("/api/v1/nodes", BackupClass[].class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().length).isEqualTo(2);
    }

    // Keep this only if /example exists and returns exactly "Here is a string"
    @Test
    void exampleEndpoint_containsString() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/example", String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isEqualTo("Here is a string");
    }
}