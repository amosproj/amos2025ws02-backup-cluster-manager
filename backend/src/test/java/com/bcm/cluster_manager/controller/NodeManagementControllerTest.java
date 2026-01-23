package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.NodeManagementService;
import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "cluster_manager"})
@Testcontainers
class NodeManagementControllerTest {

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
                .authorities(new SimpleGrantedAuthority("node:create"))
                .build();

        return WebTestClient
                .bindToApplicationContext(context)
                .apply(SecurityMockServerConfigurers.springSecurity())
                .configureClient()
                .build()
                .mutateWith(SecurityMockServerConfigurers.mockUser(user));
    }

    @Test
    void register_shouldReturnJsonStatusOk() {
        RegisterRequest request = new RegisterRequest("node1:8081", NodeMode.NODE);

        when(nodeManagementService.registerNode(any(RegisterRequest.class)))
                .thenReturn(Mono.empty());

        authenticatedClient().post()
                .uri("/api/v1/cm/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("OK");

        verify(nodeManagementService, times(1)).registerNode(any(RegisterRequest.class));
    }

    @Test
    void register_shouldReturnJsonError_whenServiceFails() {
        String errorMessage = "Something wrong";
        when(nodeManagementService.registerNode(any(RegisterRequest.class)))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        RegisterRequest request = new RegisterRequest("bad-node", NodeMode.NODE);

        authenticatedClient().post()
                .uri("/api/v1/cm/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo(errorMessage);

        verify(nodeManagementService, times(1)).registerNode(any(RegisterRequest.class));
    }
}
