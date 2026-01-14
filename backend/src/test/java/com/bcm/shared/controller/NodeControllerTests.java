package com.bcm.shared.controller;


import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Skipping Spring context startup for now")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NodeControllerTests {
    /*
    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private LocalTablesService tables;

    @Test
    void testEndpointContainsString() {
        String response = restTemplate.getForObject("/api/v1/example", String.class);
        assert response.equals("Here is a string");
    }

    @Test
    void pingEndpoint_returnsPong() {
        String response = restTemplate.getForObject("/api/v1/ping", String.class);
        assert response.equals("pong");
    }

    @Test
    void syncEndpoint_callsReplaceAllOnLocalTablesService() {
        ClusterTablesDTO dto = new ClusterTablesDTO();
        dto.setActive(List.of(new NodeDTO(1L, "Node A","10.100.179.80:9300", com.bcm.shared.model.api.NodeStatus.ACTIVE, NodeMode.NODE, LocalDateTime.now().minusDays(1)),
                new NodeDTO(2L, "Node B","10.100.179.80:9300", com.bcm.shared.model.api.NodeStatus.ACTIVE, NodeMode.NODE, LocalDateTime.now().minusDays(1))));
        dto.setInactive(List.of(new NodeDTO(3L, "Node C","10.100.179.80:9300", com.bcm.shared.model.api.NodeStatus.INACTIVE, NodeMode.NODE, LocalDateTime.now().minusDays(1))));

        HttpEntity<ClusterTablesDTO> request = new HttpEntity<>(dto);

        ResponseEntity<Void> response = restTemplate.postForEntity("/api/v1/sync", request, Void.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);

        verify(tables, times(1)).replaceAll(
                anyList(),
                anyList()
        );
    }



     */
}