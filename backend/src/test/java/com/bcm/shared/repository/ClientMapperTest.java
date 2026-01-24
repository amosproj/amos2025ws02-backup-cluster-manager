package com.bcm.shared.repository;

import com.bcm.shared.model.database.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.bcm.test.AbstractBnDbTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for the ClientMapper class to ensure proper database access functionality.
 * This focuses on validating the `findById` method, which retrieves a client by its ID.
 */
@SpringBootTest
@ActiveProfiles("test")
class ClientMapperTest extends AbstractBnDbTest {

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