package com.bcm.shared.repository;

import com.bcm.shared.model.database.Client;
import com.bcm.shared.model.database.Task;
import com.bcm.shared.model.database.TaskFrequency;
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

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class TaskMapperTest {

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
    private TaskMapper taskMapper;

    private Task createTestTask(Long clientId) {
        Task t = new Task();
        t.setName("Test Task");
        t.setClientId(clientId);
        t.setSource("JUnit");
        t.setEnabled(true);
        t.setInterval(TaskFrequency.DAILY);


        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        t.setCreatedAt(now);
        t.setUpdatedAt(now);
        return t;
    }


    @Autowired
    private ClientMapper clientMapper;

    private Mono<Client> createTestClient() {
        Client c = new Client();
        c.setNameOrIp("test-client-" + System.currentTimeMillis());
        c.setEnabled(true);
        Instant now = Instant.now();
        c.setCreatedAt(now);
        c.setUpdatedAt(now);
        return clientMapper.save(c);
    }

    private Mono<Task> insertTask(Task task) {
        return taskMapper.insertAndReturnId(
                        task.getName(),
                        task.getClientId(),
                        task.getSource(),
                        task.isEnabled(),
                        task.getInterval().name()
                )
                .flatMap(taskMapper::findById);
    }


    @Test
    void insertAndFindById_shouldPersistAndLoadTask() {
        Mono<Task> flow =
                createTestClient()
                        .flatMap(client -> {
                            Task task = createTestTask(client.getId());
                            return insertTask(task);
                        });

        StepVerifier.create(flow)
                .assertNext(loaded -> {
                    assertThat(loaded.getId()).isNotNull();
                    assertThat(loaded.getName()).isEqualTo("Test Task");
                    assertThat(loaded.getSource()).isEqualTo("JUnit");
                    assertThat(loaded.isEnabled()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void findByClient_shouldReturnTasksForClient() {
        Mono<Long> clientIdMono = createTestClient().map(Client::getId);

        Mono<java.util.List<Task>> flow =
                clientIdMono.flatMap(clientId -> {
                    Task t1 = createTestTask(clientId);
                    Task t2 = createTestTask(clientId);
                    t2.setName("Second Task");

                    return insertTask(t1)
                            .then(insertTask(t2))
                            .thenMany(taskMapper.findByClient(clientId))
                            .collectList();
                });

        StepVerifier.create(flow)
                .assertNext(tasks -> {
                    assertThat(tasks).isNotEmpty();
                    assertThat(tasks).extracting(Task::getClientId).containsOnly(tasks.get(0).getClientId());
                    assertThat(tasks).extracting(Task::getName).contains("Test Task", "Second Task");
                })
                .verifyComplete();
    }

    @Test
    void update_shouldModifyExistingTask() {
        Mono<Task> flow =
                createTestClient()
                        .flatMap(client -> insertTask(createTestTask(client.getId())))
                        .flatMap(saved ->
                                taskMapper.findById(saved.getId())
                                        .flatMap(before -> {
                                            before.setName("Updated Name");
                                            before.setEnabled(false);
                                            before.setUpdatedAt(Instant.now());
                                            return taskMapper.update( before.getId(), before.getName(), before.getClientId(), before.getSource(), before.isEnabled(), before.getUpdatedAt(), before.getInterval().name())
                                                    .then(taskMapper.findById(before.getId()));
                                        })
                        );

        StepVerifier.create(flow)
                .assertNext(after -> {
                    assertThat(after.getName()).isEqualTo("Updated Name");
                    assertThat(after.isEnabled()).isFalse();
                })
                .verifyComplete();
    }


    @Test
    void delete_shouldRemoveTask() {
        Mono<Void> flow =
                createTestClient()
                        .flatMap(client -> insertTask(createTestTask(client.getId())))
                        .flatMap(saved -> taskMapper.deleteById(saved.getId()).then())
                        .then(); // just complete if delete succeeded

        StepVerifier.create(flow)
                .verifyComplete();
    }
}

