package com.bcm.cluster_manager.repository;

import com.bcm.shared.model.database.User;
import com.bcm.shared.repository.UserMapper;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class UserRepositoryTest {

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
    private UserMapper userMapper;

    @Test
    void testInsertAndPersists() {
        User user = new User();
        user.setName("testuser_" + System.currentTimeMillis());
        user.setPasswordHash("hashedpwd");
        user.setEnabled(true);
        user.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        user.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        Mono<User> persisted = userMapper.save(user)
                .flatMap(saved -> userMapper.findUserById(saved.getId()));

        StepVerifier.create(persisted)
                .assertNext(found -> {
                    assertNotNull(found.getId(), "User sollte eine ID haben");
                    assertEquals(user.getName(), found.getName(), "Namen sollten übereinstimmen");
                    assertEquals(user.getPasswordHash(), found.getPasswordHash(), "Password Hash sollte übereinstimmen");
                    assertEquals(user.isEnabled(), found.isEnabled(), "Enabled Flag sollte übereinstimmen");
                })
                .verifyComplete();
    }
}
