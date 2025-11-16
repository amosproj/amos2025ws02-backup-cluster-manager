package com.bcm.shared.repository;

import com.bcm.shared.model.database.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for the ClientMapper class to ensure proper database access functionality.
 * This focuses on validating the `findById` method, which retrieves a client by its ID.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Transactional
@Rollback
class ClientMapperTest {

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
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        client.setCreatedAt(now);
        client.setUpdatedAt(now);
        clientMapper.insert(client);
        return client;
    }

    @Test
    void findById_shouldReturnClientForValidId() {
        // Arrange
        Client createdClient = createTestClient();

        // Act
        Client foundClient = clientMapper.findById(createdClient.getId());

        // Assert
        assertThat(foundClient).isNotNull();
        assertThat(foundClient.getId()).isEqualTo(createdClient.getId());
        assertThat(foundClient.getNameOrIp()).isEqualTo(createdClient.getNameOrIp());
        assertThat(foundClient.isEnabled()).isEqualTo(createdClient.isEnabled());
        assertThat(foundClient.getCreatedAt()).isEqualTo(createdClient.getCreatedAt());
        assertThat(foundClient.getUpdatedAt()).isEqualTo(createdClient.getUpdatedAt());
    }

    @Test
    void findById_shouldReturnNullForInvalidId() {
        // Act
        Client foundClient = clientMapper.findById(-1L); // Non-existent ID

        // Assert
        assertThat(foundClient).isNull();
    }
}