package com.bcm.cluster_manager.repository;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.repository.GroupMapper;
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
 * Test suite for the GroupMapper class to ensure proper database access functionality.
 * These tests focus on validating the `findById` method, which retrieves a group by its ID.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class GroupMapperTest {

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
    private GroupMapper groupMapper;

    /**
     * Creates and persists a new test group instance.
     * The group is initialized with a unique name, enabled status, and the current timestamp
     * for both creation and update times. The group is then inserted into the database.
     *
     * @return the created and persisted test group instance
     */
    private Mono<Group> createTestGroup() {
        Group group = new Group();
        group.setName("Superuser");
        group.setEnabled(true);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        group.setCreatedAt(now);
        group.setUpdatedAt(now);
        return groupMapper.save(group);
    }

    @Test
    void findById_shouldReturnGroupForValidId() {
        Mono<Group> flow = createTestGroup()
                .flatMap(group -> groupMapper.findById(group.getId()));
        StepVerifier.create(flow)
                .assertNext(arr -> {
                    assertThat(arr).isNotNull();
                    assertThat(arr.getName()).isEqualTo("Superuser");
                    assertThat(arr.isEnabled()).isTrue();
                    assertThat(arr.getCreatedAt()).isNotNull();
                }).verifyComplete();
    }

    @Test
    void findById_shouldReturnNullForInvalidId() {
        StepVerifier.create(groupMapper.findById(-1L))
                .verifyComplete();
    }
}