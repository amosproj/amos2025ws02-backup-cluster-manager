package com.bcm.shared.repository;

import com.bcm.shared.model.database.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for the BackupMapper class to ensure proper database access functionality.
 * This focuses on validating the `findById` method, which retrieves a backup by its ID.
 */
@SpringBootTest
@Testcontainers
@Disabled("Skipping Spring context startup for now")
class BackupMapperTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private BackupMapper backupMapper;

    /**
     * Creates and persists a new test backup instance.
     * The backup is initialized with required fields like clientId, taskId, startTime, sizeBytes,
     * state, message, and createdAt. The backup is then inserted into the database.
     *
     * @return the created and persisted test backup instance
     */
    private Mono<Backup> createTestBackup(Long clientId, Long taskId) {
        Backup backup = new Backup();
        backup.setClientId(clientId);
        backup.setTaskId(taskId);
        backup.setStartTime(Instant.now().truncatedTo(ChronoUnit.MICROS));
        backup.setStopTime(Instant.now().truncatedTo(ChronoUnit.MICROS).plusSeconds(3600)); // 1 hour later
        backup.setSizeBytes(1024L);
        backup.setState(BackupState.COMPLETED);
        backup.setMessage("Test backup message");
        backup.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        return backupMapper.save(backup);
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
        Instant now = Instant.now();
        client.setCreatedAt(now);
        client.setUpdatedAt(now);
        return clientMapper.save(client);
    }

    @Autowired
    private TaskMapper taskMapper;

    /**
     * Creates and persists a new test Task instance.
     * The Task is initialized with predefined attributes such as name, clientId, source, enabled status,
     * and timestamps for creation and updates. The instance is then inserted into the database.
     *
     * @param clientId the ID of the client to associate with the created Task
     * @return the created and persisted Task instance
     */
    private Mono<Task> createTestTask(Long clientId) {
        Task task = new Task();
        task.setName("Test Task");
        task.setClientId(clientId);
        task.setSource("JUnit");
        task.setEnabled(true);
        task.setInterval(TaskFrequency.DAILY);

        // Falls dein Task andere Zeittypen nutzt (z.B. Instant/OffsetDateTime), bitte anpassen:
        Instant now = Instant.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        return taskMapper.save(task);
    }

    @Test
    void findById_shouldReturnBackupForValidId() {
        StepVerifier.create(
                createTestClient()
                        .flatMap(client -> createTestTask(client.getId())
                                .flatMap(task -> createTestBackup(client.getId(), task.getId()))
                        )
                        .flatMap(createdBackup -> backupMapper.findById(createdBackup.getId())
                                .doOnNext(foundBackup -> {
                                    assertThat(foundBackup).isNotNull();
                                    assertThat(foundBackup.getId()).isEqualTo(createdBackup.getId());
                                    assertThat(foundBackup.getClientId()).isEqualTo(createdBackup.getClientId());
                                    assertThat(foundBackup.getTaskId()).isEqualTo(createdBackup.getTaskId());
                                    assertThat(foundBackup.getStartTime()).isEqualTo(createdBackup.getStartTime());
                                    assertThat(foundBackup.getStopTime()).isEqualTo(createdBackup.getStopTime());
                                    assertThat(foundBackup.getSizeBytes()).isEqualTo(createdBackup.getSizeBytes());
                                    assertThat(foundBackup.getState()).isEqualTo(createdBackup.getState());
                                    assertThat(foundBackup.getMessage()).isEqualTo(createdBackup.getMessage());
                                    assertThat(foundBackup.getCreatedAt()).isEqualTo(createdBackup.getCreatedAt());
                                })
                        )
        ).expectNextCount(1).verifyComplete();
    }

    @Test
    void findById_shouldReturnEmptyForInvalidId() {
        StepVerifier.create(backupMapper.findById(-1L))
                .verifyComplete();
    }

    @Test
    void save_shouldPersistBackupAndGenerateId() {
        StepVerifier.create(
                createTestClient()
                        .flatMap(client -> createTestTask(client.getId())
                                .flatMap(task -> {
                                    Backup backup = new Backup();
                                    backup.setClientId(client.getId());
                                    backup.setTaskId(task.getId());

                                    Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
                                    backup.setStartTime(now);
                                    backup.setStopTime(now.plusSeconds(3600));
                                    backup.setSizeBytes(42L);
                                    backup.setState(BackupState.COMPLETED);
                                    backup.setMessage("insert integration test");
                                    backup.setCreatedAt(now);

                                    return backupMapper.save(backup)
                                            .flatMap(saved -> backupMapper.findById(saved.getId())
                                                    .doOnNext(persisted -> {
                                                        assertThat(persisted).isNotNull();
                                                        assertThat(persisted.getId()).isNotNull();
                                                        assertThat(persisted.getClientId()).isEqualTo(client.getId());
                                                        assertThat(persisted.getTaskId()).isEqualTo(task.getId());
                                                        assertThat(persisted.getStartTime()).isEqualTo(backup.getStartTime());
                                                        assertThat(persisted.getStopTime()).isEqualTo(backup.getStopTime());
                                                        assertThat(persisted.getSizeBytes()).isEqualTo(backup.getSizeBytes());
                                                        assertThat(persisted.getState()).isEqualTo(BackupState.COMPLETED);
                                                        assertThat(persisted.getMessage()).isEqualTo(backup.getMessage());
                                                        assertThat(persisted.getCreatedAt()).isEqualTo(backup.getCreatedAt());
                                                    })
                                            );
                                })
                        )
        ).expectNextCount(1).verifyComplete();
    }
}