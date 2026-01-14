package com.bcm.shared.repository;

import com.bcm.shared.model.database.Client;
import com.bcm.shared.model.database.Task;
import com.bcm.shared.model.database.TaskFrequency;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

@DataR2dbcTest
@Disabled("Skipping Spring context startup for now")
class TaskMapperTest {


    @Autowired
    private TaskMapper taskMapper;

    private Task createTestTask(Long clientId) {
        Task t = new Task();
        t.setName("Test Task");
        t.setClientId(clientId);
        t.setSource("JUnit");
        t.setEnabled(true);
        t.setInterval(TaskFrequency.DAILY);

        // Falls dein Task andere Zeittypen nutzt (z.B. Instant/OffsetDateTime), bitte anpassen:
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


    @Test
    void insertAndFindById_shouldPersistAndLoadTask() {
        Mono<Task> flow =
                createTestClient()
                        .flatMap(client -> {
                            Task task = createTestTask(client.getId());
                            return taskMapper.save(task)
                                    .flatMap(saved -> taskMapper.findById(saved.getId()));
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

                    return taskMapper.save(t1)
                            .then(taskMapper.save(t2))
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
                        .flatMap(client -> taskMapper.save(createTestTask(client.getId())))
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
                        .flatMap(client -> taskMapper.save(createTestTask(client.getId())))
                        .flatMap(saved -> taskMapper.deleteById(saved.getId()).then())
                        .then(); // just complete if delete succeeded

        StepVerifier.create(flow)
                .verifyComplete();
    }
}

