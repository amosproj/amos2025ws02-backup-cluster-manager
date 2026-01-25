package com.bcm.shared.service;

import com.bcm.shared.model.api.CacheInvalidationType;
import com.bcm.shared.model.api.ExecuteBackupRequest;
import com.bcm.shared.model.database.Backup;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.model.database.Client;
import com.bcm.shared.model.database.Task;
import com.bcm.shared.model.database.TaskFrequency;
import com.bcm.shared.repository.BackupMapper;
import com.bcm.shared.repository.ClientMapper;
import com.bcm.shared.repository.TaskMapper;
import com.bcm.test.AbstractBnDbTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BackupNodeServiceTests extends AbstractBnDbTest {

    @Autowired
    private BackupService backupService;

    @Autowired
    private BackupMapper backupMapper;

    @Autowired
    private ClientMapper clientMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private CacheEventStore eventStore;

    @Autowired
    @Qualifier("bnTemplate")
    private R2dbcEntityTemplate bnTemplate;

    @BeforeEach
    void setup() {
        // Clean up backups before each test
        backupMapper.deleteAll().block();
    }

    @Test
    void executeBackupSync_shouldCompleteSuccessfully() {

        StepVerifier.create(
                createTestClient()
                        .flatMap(client -> createTestTask(client.getId())
                                .flatMap(task -> createTestBackup(client.getId(), task.getId(), BackupState.QUEUED))
                                .flatMap(backup -> {

                                    ExecuteBackupRequest request = new ExecuteBackupRequest();
                                    request.setDuration(100L);
                                    request.setShouldSucceed(true);

                                    return backupService.executeBackupSync(backup.getId(), request)
                                            .then(backupMapper.findById(backup.getId()))
                                            .doOnNext(completed -> {

                                                assertThat(completed).isNotNull();
                                                assertThat(completed.getState()).isEqualTo(BackupState.COMPLETED);
                                                assertThat(completed.getMessage()).isEqualTo("Backup completed successfully.");
                                                assertThat(completed.getStopTime()).isNotNull();
                                                assertThat(completed.getStopTime()).isAfter(completed.getStartTime());

                                                var events = eventStore.getAllUnprocessedEvents();
                                                assertThat(events).anyMatch(e ->
                                                        e.getType() == CacheInvalidationType.BACKUP_UPDATED &&
                                                                e.getEntityId().equals(backup.getId())
                                                );
                                            });
                                })
                        )
        ).expectNextCount(1).verifyComplete();
    }

    @Test
    void executeBackupSync_shouldFailWhenRequested() {

        StepVerifier.create(
                createTestClient()
                        .flatMap(client -> createTestTask(client.getId())
                                .flatMap(task -> createTestBackup(client.getId(), task.getId(), BackupState.QUEUED))
                                .flatMap(backup -> {
                                    ExecuteBackupRequest request = new ExecuteBackupRequest();
                                    request.setDuration(100L);
                                    request.setShouldSucceed(false);

                                    return backupService.executeBackupSync(backup.getId(), request)
                                            .then(backupMapper.findById(backup.getId()))
                                            .doOnNext(failed -> {
                                                assertThat(failed).isNotNull();
                                                assertThat(failed.getState()).isEqualTo(BackupState.FAILED);
                                                assertThat(failed.getMessage()).isEqualTo("Backup failed due to an error.");
                                                assertThat(failed.getStopTime()).isNotNull();

                                                var events = eventStore.getAllUnprocessedEvents();
                                                assertThat(events).anyMatch(e ->
                                                        e.getType() == CacheInvalidationType.BACKUP_UPDATED &&
                                                                e.getEntityId().equals(backup.getId())
                                                );
                                            });
                                })
                        )
        ).expectNextCount(1).verifyComplete();
    }

    @Test
    void executeBackupSync_shouldRespectDuration() {

        long startTime = System.currentTimeMillis();

        StepVerifier.create(
                createTestClient()
                        .flatMap(client -> createTestTask(client.getId())
                                .flatMap(task -> createTestBackup(client.getId(), task.getId(), BackupState.QUEUED))
                                .flatMap(backup -> {

                                    ExecuteBackupRequest request = new ExecuteBackupRequest();
                                    request.setDuration(200L);
                                    request.setShouldSucceed(true);

                                    return backupService.executeBackupSync(backup.getId(), request)
                                            .then(Mono.just(backup));
                                })
                        )
        ).expectNextCount(1).verifyComplete();

        long endTime = System.currentTimeMillis();
        long actualDuration = endTime - startTime;
        assertThat(actualDuration).isGreaterThanOrEqualTo(200L);
    }

    private Mono<Client> createTestClient() {
        Client client = new Client();
        client.setNameOrIp("test-client-" + System.currentTimeMillis());
        client.setEnabled(true);
        Instant now = Instant.now();
        client.setCreatedAt(now);
        client.setUpdatedAt(now);
        return clientMapper.save(client);
    }

    private Mono<Task> createTestTask(Long clientId) {
        Task task = new Task();
        task.setName("Test Task");
        task.setClientId(clientId);
        task.setSource("JUnit");
        task.setEnabled(true);
        task.setInterval(TaskFrequency.DAILY);

        Instant now = Instant.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        return taskMapper.insertAndReturnId(
                        task.getName(),
                        task.getClientId(),
                        task.getSource(),
                        task.isEnabled(),
                        task.getInterval().name()
                )
                .flatMap(taskMapper::findById);
    }

    private Mono<Backup> createTestBackup(Long clientId, Long taskId, BackupState state) {
        Backup backup = new Backup();
        backup.setClientId(clientId);
        backup.setTaskId(taskId);
        backup.setStartTime(Instant.now().truncatedTo(ChronoUnit.MICROS));
        backup.setSizeBytes(1024L);
        backup.setState(state);
        backup.setMessage("Test backup message");
        backup.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        return insertBackup(backup).flatMap(backupMapper::findById);
    }

    private Mono<Long> insertBackup(Backup backup) {
        return bnTemplate.getDatabaseClient()
                .sql("""
                        INSERT INTO backups (client_id, task_id, start_time, size_bytes, state, message, created_at)
                        VALUES (:clientId, :taskId, :startTime, :sizeBytes, :state::backup_state, :message, :createdAt)
                        RETURNING id
                        """)
                .bind("clientId", backup.getClientId())
                .bind("taskId", backup.getTaskId())
                .bind("startTime", backup.getStartTime())
                .bind("sizeBytes", backup.getSizeBytes())
                .bind("state", backup.getState().name())
                .bind("message", backup.getMessage())
                .bind("createdAt", backup.getCreatedAt())
                .map((row, metadata) -> row.get("id", Long.class))
                .one();
    }
}