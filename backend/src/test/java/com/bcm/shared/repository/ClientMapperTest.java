package com.bcm.shared.repository;

import com.bcm.shared.model.database.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for the ClientMapper class to ensure proper database access functionality.
 * This focuses on validating the `findById` method, which retrieves a client by its ID.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ClientMapperTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("postgres")
            .withUsername("appuser")
            .withPassword("apppassword");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        try (Connection conn = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("CREATE DATABASE bcm_node0 OWNER " + postgres.getUsername());
            } catch (Exception e) {
                if (!e.getMessage().contains("already exists")) throw new RuntimeException("Failed to create bcm_node0", e);
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
        registry.add("spring.r2dbc.bn.url", () -> String.format("r2dbc:postgresql://%s:%d/bcm_node0", h, p));
        registry.add("spring.r2dbc.bn.username", () -> u);
        registry.add("spring.r2dbc.bn.password", () -> pw);
    }

    @Autowired
    private ClientMapper clientMapper;

    /**
     * Creates and persists a new test client instance.
     * The client is initialized with a unique name or IP, enabled status, and the current timestamp
     * for both creation and update times. The client is then inserted into the database.
     *
     * @return the created and persisted test client instance
     */
private Mono<Client> createTestClient() {
        Client client = new Client();
        client.setNameOrIp("test-client-" + System.currentTimeMillis());
        client.setEnabled(true);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        client.setCreatedAt(now);
        client.setUpdatedAt(now);
        return clientMapper.save(client);
    }

    @Test
    void findById_shouldReturnClientForValidId() {
            Mono<Client> testClient = createTestClient()
                    .flatMap(savedClient -> clientMapper.findById(savedClient.getId()));
            StepVerifier.create(testClient)
                    .assertNext(foundClient -> {
                        assertThat(foundClient).isNotNull();
                        assertThat(foundClient.getId()).isNotNull();
                        assertThat(foundClient.isEnabled()).isTrue();
                        assertThat(foundClient.getCreatedAt()).isNotNull();
                        assertThat(foundClient.getUpdatedAt()).isNotNull();
                    }).verifyComplete();
    }
    @Test
    void findById_shouldReturnNullForInvalidId() {
        StepVerifier.create(clientMapper.findById(-1L))
                .expectNextCount(0)
                .verifyComplete();
    }
}