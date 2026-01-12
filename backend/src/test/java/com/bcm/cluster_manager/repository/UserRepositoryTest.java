package com.bcm.cluster_manager.repository;

import com.bcm.shared.model.database.User;
import com.bcm.shared.repository.UserMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.ANY) // use in-memory DB for tests
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("Skipping Spring context startup for now")
public class UserRepositoryTest {

    @Qualifier("userMapperBN")
    @Autowired
    private UserMapper userRepository;

    @BeforeAll
    void setup() {
        // Optional: ensure schema is present if not managed by migrations
    }

    @Test
    void testInsertAndPersists() {
        User user = new User();
        user.setName("testuser_" + System.currentTimeMillis());
        user.setPasswordHash("hashedpwd");
        user.setEnabled(true);
        user.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        user.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        StepVerifier.create(userRepository.save(user))
                .assertNext(savedUser -> {
                    assertNotNull(savedUser.getId(), "User sollte eine ID haben");
                    System.out.println("User gespeichert mit ID: " + savedUser.getId());

                    Mono<User> persisted = userRepository.findUserById(savedUser.getId());

                    StepVerifier.create(persisted)
                            .assertNext(found -> {
                                assertEquals(user.getName(), found.getName(), "Namen sollten übereinstimmen");
                                assertEquals(user.getPasswordHash(), found.getPasswordHash(), "Password Hash sollte übereinstimmen");
                                assertEquals(user.isEnabled(), found.isEnabled(), "Enabled Flag sollte übereinstimmen");
                            })
                            .verifyComplete();
                })
                .verifyComplete();
    }
}

// Additional tests (update, delete, retrieval) can follow