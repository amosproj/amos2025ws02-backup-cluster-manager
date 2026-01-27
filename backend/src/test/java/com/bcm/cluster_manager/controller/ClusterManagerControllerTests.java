package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.NodeManagementService;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.NodeStatus;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "cluster_manager"})
@Testcontainers
class ClusterManagerControllerTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("postgres")
            .withUsername("appuser")
            .withPassword("apppassword");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        try (Connection conn = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement stmt = conn.createStatement()) {
            for (String db : new String[]{"bcm_node0", "bcm"}) {
                try {
                    stmt.executeUpdate("CREATE DATABASE " + db + " OWNER " + postgres.getUsername());
                } catch (Exception e) {
                    if (!e.getMessage().contains("already exists")) throw new RuntimeException("Failed to create " + db, e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Database creation failed", e);
        }

        String h = postgres.getHost();
        int p = postgres.getFirstMappedPort();
        String u = postgres.getUsername();
        String pw = postgres.getPassword();

        registry.add("spring.datasource.hikari.jdbc-url", () -> String.format("jdbc:postgresql://%s:%d/bcm_node0", h, p));
        registry.add("spring.datasource.hikari.username", () -> u);
        registry.add("spring.datasource.hikari.password", () -> pw);
        registry.add("spring.cm-datasource.hikari.jdbc-url", () -> String.format("jdbc:postgresql://%s:%d/bcm", h, p));
        registry.add("spring.cm-datasource.hikari.username", () -> u);
        registry.add("spring.cm-datasource.hikari.password", () -> pw);
        registry.add("spring.r2dbc.bn.url", () -> String.format("r2dbc:postgresql://%s:%d/bcm_node0", h, p));
        registry.add("spring.r2dbc.bn.username", () -> u);
        registry.add("spring.r2dbc.bn.password", () -> pw);
        registry.add("spring.r2dbc.cm.url", () -> String.format("r2dbc:postgresql://%s:%d/bcm", h, p));
        registry.add("spring.r2dbc.cm.username", () -> u);
        registry.add("spring.r2dbc.cm.password", () -> pw);
    }

    @MockitoBean
    private NodeManagementService nodeManagementService;

    @Autowired
    private ApplicationContext context;

    private WebTestClient authenticatedClient() {
        UserDetails user = User.withUsername("testuser")
                .password("password")
                .authorities(new SimpleGrantedAuthority("node:read"))
                .build();

        return WebTestClient
                .bindToApplicationContext(context)
                .apply(SecurityMockServerConfigurers.springSecurity())
                .configureClient()
                .build()
                .mutateWith(SecurityMockServerConfigurers.mockUser(user));
    }

    @Test
    void nodesEndpoint_returnsList() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "10.0.0.1:8080", NodeStatus.ACTIVE, NodeMode.NODE, true, LocalDateTime.now().minusDays(1));
        NodeDTO n2 = new NodeDTO(2L, "Node B", "10.0.0.2:8080", NodeStatus.INACTIVE, NodeMode.NODE, true, LocalDateTime.now().minusDays(2));

        PaginationResponse<NodeDTO> response = new PaginationResponse<>(List.of(n1, n2), 1, 1, 2);
        when(nodeManagementService.getPaginatedItems(any(PaginationRequest.class)))
                .thenReturn(Mono.just(response));

        authenticatedClient().get()
                .uri("/api/v1/cm/nodes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items").isArray()
                .jsonPath("$.items.length()").isEqualTo(2)
                .jsonPath("$.totalItems").isEqualTo(2);

        verify(nodeManagementService, times(1)).getPaginatedItems(any(PaginationRequest.class));
    }

    @Test
    void nodesEndpoint_withActiveFilter_returnsFilteredList() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "10.0.0.1:8080", NodeStatus.ACTIVE, NodeMode.NODE, true, LocalDateTime.now().minusDays(1));

        PaginationResponse<NodeDTO> response = new PaginationResponse<>(List.of(n1), 1, 1, 1);
        when(nodeManagementService.getPaginatedItems(any(PaginationRequest.class)))
                .thenReturn(Mono.just(response));

        authenticatedClient().get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/cm/nodes")
                        .queryParam("filters", "ACTIVE")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items").isArray()
                .jsonPath("$.items.length()").isEqualTo(1)
                .jsonPath("$.items[0].status").isEqualTo("active");

        verify(nodeManagementService, times(1)).getPaginatedItems(any(PaginationRequest.class));
    }

    @Test
    void nodesEndpoint_withSearch_returnsMatchingNodes() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "10.0.0.1:8080", NodeStatus.ACTIVE, NodeMode.NODE, true, LocalDateTime.now().minusDays(1));

        PaginationResponse<NodeDTO> response = new PaginationResponse<>(List.of(n1), 1, 1, 1);
        when(nodeManagementService.getPaginatedItems(any(PaginationRequest.class)))
                .thenReturn(Mono.just(response));

        authenticatedClient().get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/cm/nodes")
                        .queryParam("search", "Node A")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items").isArray()
                .jsonPath("$.items.length()").isEqualTo(1)
                .jsonPath("$.items[0].name").isEqualTo("Node A");

        verify(nodeManagementService, times(1)).getPaginatedItems(any(PaginationRequest.class));
    }

    @Test
    void nodesEndpoint_withActiveAndSearch_returnsCombinedFilter() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "10.0.0.1:8080", NodeStatus.ACTIVE, NodeMode.NODE, true, LocalDateTime.now().minusDays(1));

        PaginationResponse<NodeDTO> response = new PaginationResponse<>(List.of(n1), 1, 1, 1);
        when(nodeManagementService.getPaginatedItems(any(PaginationRequest.class)))
                .thenReturn(Mono.just(response));

        authenticatedClient().get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/cm/nodes")
                        .queryParam("filters", "ACTIVE")
                        .queryParam("search", "Node A")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items").isArray()
                .jsonPath("$.items.length()").isEqualTo(1)
                .jsonPath("$.items[0].name").isEqualTo("Node A")
                .jsonPath("$.items[0].status").isEqualTo("active");

        verify(nodeManagementService, times(1)).getPaginatedItems(any(PaginationRequest.class));
    }

    @Test
    void nodesEndpoint_withSearchById_returnsNode() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "10.0.0.1:8080", NodeStatus.ACTIVE, NodeMode.NODE, true, LocalDateTime.now().minusDays(1));

        PaginationResponse<NodeDTO> response = new PaginationResponse<>(List.of(n1), 1, 1, 1);
        when(nodeManagementService.getPaginatedItems(any(PaginationRequest.class)))
                .thenReturn(Mono.just(response));

        authenticatedClient().get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/cm/nodes")
                        .queryParam("search", "1")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items").isArray()
                .jsonPath("$.items.length()").isEqualTo(1)
                .jsonPath("$.items[0].id").isEqualTo(1);

        verify(nodeManagementService, times(1)).getPaginatedItems(any(PaginationRequest.class));
    }

    @Test
    void nodesEndpoint_noMatches_returnsEmptyList() {
        PaginationResponse<NodeDTO> response = new PaginationResponse<>(List.of(), 1, 1, 0);
        when(nodeManagementService.getPaginatedItems(any(PaginationRequest.class)))
                .thenReturn(Mono.just(response));

        authenticatedClient().get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/cm/nodes")
                        .queryParam("filters", "ACTIVE")
                        .queryParam("search", "NonExistent")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items").isArray()
                .jsonPath("$.items.length()").isEqualTo(0)
                .jsonPath("$.totalItems").isEqualTo(0);

        verify(nodeManagementService, times(1)).getPaginatedItems(any(PaginationRequest.class));
    }

    @Test
    void nodesEndpoint_withEmptySearch_treatsAsNull() {
        NodeDTO n1 = new NodeDTO(1L, "Node A", "10.0.0.1:8080", NodeStatus.ACTIVE, NodeMode.NODE, true, LocalDateTime.now().minusDays(1));
        NodeDTO n2 = new NodeDTO(2L, "Node B", "10.0.0.2:8080", NodeStatus.INACTIVE, NodeMode.NODE, true, LocalDateTime.now().minusDays(2));

        PaginationResponse<NodeDTO> response = new PaginationResponse<>(List.of(n1, n2), 1, 1, 2);
        when(nodeManagementService.getPaginatedItems(any(PaginationRequest.class)))
                .thenReturn(Mono.just(response));

        authenticatedClient().get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/cm/nodes")
                        .queryParam("search", "")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        verify(nodeManagementService, times(1)).getPaginatedItems(any(PaginationRequest.class));
    }
}
