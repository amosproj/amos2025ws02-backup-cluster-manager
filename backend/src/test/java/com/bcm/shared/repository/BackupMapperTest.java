package com.bcm.shared.repository;

import com.bcm.shared.model.database.*;
import com.bcm.shared.repository.BackupMapper;
import com.bcm.shared.repository.ClientMapper;
import com.bcm.shared.repository.TaskMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for the BackupMapper class to ensure proper database access functionality.
 * This focuses on validating the `findById` method, which retrieves a backup by its ID.
 */
@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional
@Rollback
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
    private Backup createTestBackup(Long clientId, Long taskId) {
        Backup backup = new Backup();
        backup.setClientId(clientId);
        backup.setTaskId(taskId);
        backup.setStartTime(Instant.now().truncatedTo(ChronoUnit.MICROS));
        backup.setStopTime(Instant.now().truncatedTo(ChronoUnit.MICROS).plusSeconds(3600)); // 1 hour later
        backup.setSizeBytes(1024L);
        backup.setState(BackupState.COMPLETED);
        backup.setMessage("Test backup message");
        backup.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        backupMapper.insert(backup);
        return backup;
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
    private Client createTestClient() {
        Client client = new Client();
        client.setNameOrIp("test-client-" + System.currentTimeMillis());
        client.setEnabled(true);
        Instant now = Instant.now();
        client.setCreatedAt(now);
        client.setUpdatedAt(now);
        clientMapper.insert(client);
        return client;
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
    private Task createTestTask(Long clientId) {
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
        taskMapper.insert(task);
        return task;
    }

    @Test
    void findById_shouldReturnBackupForValidId() {
        Client client = createTestClient();
        Task task = createTestTask(client.getId());

        // Arrange
        Backup createdBackup = createTestBackup(client.getId(), task.getId());

        // Act
        Backup foundBackup = backupMapper.findById(createdBackup.getId());

        // Assert
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
    }

    @Test
    void findById_shouldReturnNullForInvalidId() {
        // Act
        Backup foundBackup = backupMapper.findById(-1L); // Non-existent ID

        // Assert
        assertThat(foundBackup).isNull();
    }

    @Test
    void insert_shouldPersistBackupAndGenerateId() {
        // Arrange: create client + task, because backups reference both
        Client client = createTestClient();
        Task task = createTestTask(client.getId());

        Backup backup = new Backup();
        backup.setClientId(client.getId());
        backup.setTaskId(task.getId());

        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        backup.setStartTime(now);
        backup.setStopTime(now.plusSeconds(3600)); // 1 hour later
        backup.setSizeBytes(42L);
        backup.setState(BackupState.COMPLETED);   // valid value from backup_state enum
        backup.setMessage("insert integration test");
        backup.setCreatedAt(now);

        // Act
        int rows = backupMapper.insert(backup);

        // Assert: DB insert happened, ID generated, values round-trip correctly
        assertThat(rows).isEqualTo(1);
        assertThat(backup.getId()).isNotNull();

        Backup persisted = backupMapper.findById(backup.getId());
        assertThat(persisted).isNotNull();
        assertThat(persisted.getId()).isEqualTo(backup.getId());
        assertThat(persisted.getClientId()).isEqualTo(client.getId());
        assertThat(persisted.getTaskId()).isEqualTo(task.getId());
        assertThat(persisted.getStartTime()).isEqualTo(backup.getStartTime());
        assertThat(persisted.getStopTime()).isEqualTo(backup.getStopTime());
        assertThat(persisted.getSizeBytes()).isEqualTo(backup.getSizeBytes());
        assertThat(persisted.getState()).isEqualTo(BackupState.COMPLETED);
        assertThat(persisted.getMessage()).isEqualTo(backup.getMessage());
        assertThat(persisted.getCreatedAt()).isEqualTo(backup.getCreatedAt());
    }
}