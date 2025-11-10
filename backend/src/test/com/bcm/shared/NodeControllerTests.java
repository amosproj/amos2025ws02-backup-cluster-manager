package test.com.bcm.shared;

import api.model.NodeDTO;
import com.bcm.shared.NodeService;
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
class NodeControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private NodeService nodeService;

    @Test
    void nodesEndpoint_returnsList() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "Active", LocalDateTime.now().minusDays(1));
        NodeDTO n2 = new NodeDTO(2L, "Node B", "Inactive", LocalDateTime.now().minusDays(2));
        when(nodeService.getAllNodes()).thenReturn(List.of(n1, n2));

        ResponseEntity<NodeDTO[]> resp =
            restTemplate.getForEntity("/api/v1/nodes", NodeDTO[].class);

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