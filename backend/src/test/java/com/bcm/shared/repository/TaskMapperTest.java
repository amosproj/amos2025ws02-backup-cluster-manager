package com.bcm.shared.repository;

import com.bcm.shared.model.database.Client;
import com.bcm.shared.model.database.Task;
import com.bcm.shared.model.database.TaskFrequency;
import com.bcm.shared.repository.ClientMapper;
import com.bcm.shared.repository.TaskMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Transactional
@Rollback
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

    private Client createTestClient() {
        Client c = new Client();
        c.setNameOrIp("test-client-" + System.currentTimeMillis());
        c.setEnabled(true);
        Instant now = Instant.now();
        c.setCreatedAt(now);
        c.setUpdatedAt(now);
        clientMapper.insert(c);
        return c;
    }

    @Test
    void insertAndFindById_shouldPersistAndLoadTask() {
        Client client = createTestClient();
        Task task = createTestTask(client.getId());

        int rows = taskMapper.insert(task);
        assertThat(rows).isEqualTo(1);
        assertThat(task.getId()).isNotNull(); // useGeneratedKeys sollte hier greifen

        Task loaded = taskMapper.findById(task.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getId()).isEqualTo(task.getId());
        assertThat(loaded.getName()).isEqualTo("Test Task");
        assertThat(loaded.getClientId()).isEqualTo(client.getId());
        assertThat(loaded.getSource()).isEqualTo("JUnit");
        assertThat(loaded.isEnabled()).isTrue();
    }

    @Test
    void findByClient_shouldReturnTasksForClient() {
        Client client = createTestClient();

        Task t1 = createTestTask(client.getId());
        Task t2 = createTestTask(client.getId());
        t2.setName("Second Task");

        taskMapper.insert(t1);
        taskMapper.insert(t2);

        List<Task> tasks = taskMapper.findByClient(client.getId());

        assertThat(tasks)
                .isNotEmpty()
                .extracting(Task::getClientId)
                .containsOnly(client.getId());

        assertThat(tasks)
                .extracting(Task::getName)
                .contains("Test Task", "Second Task");
    }

    @Test
    void update_shouldModifyExistingTask() {
        Client client = createTestClient();
        Task task = createTestTask(client.getId());
        taskMapper.insert(task);

        Task beforeUpdate = taskMapper.findById(task.getId());
        assertThat(beforeUpdate).isNotNull();

        beforeUpdate.setName("Updated Name");
        beforeUpdate.setEnabled(false);
        beforeUpdate.setUpdatedAt(Instant.now());

        int rows = taskMapper.update(beforeUpdate);
        assertThat(rows).isEqualTo(1);

        Task afterUpdate = taskMapper.findById(task.getId());
        assertThat(afterUpdate).isNotNull();
        assertThat(afterUpdate.getName()).isEqualTo("Updated Name");
        assertThat(afterUpdate.isEnabled()).isFalse();
    }

    @Test
    void delete_shouldRemoveTask() {
        Client client = createTestClient();
        Task task = createTestTask(client.getId());
        taskMapper.insert(task);

        Long id = task.getId();
        assertThat(taskMapper.findById(id)).isNotNull();

        int rows = taskMapper.delete(id);
        assertThat(rows).isEqualTo(1);

        Task deleted = taskMapper.findById(id);
        assertThat(deleted).isNull();
    }
}

