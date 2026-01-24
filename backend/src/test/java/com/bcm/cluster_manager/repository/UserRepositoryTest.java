package com.bcm.cluster_manager.repository;

import com.bcm.shared.model.database.User;
import com.bcm.shared.repository.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.bcm.test.AbstractBnDbTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserRepositoryTest extends AbstractBnDbTest {

    @Autowired
    private UserMapper userRepository;

    @Test
    void testInsertAndPersists() {
        User user = new User();
        user.setName("testuser_" + System.currentTimeMillis());
        user.setPasswordHash("hashedpwd");
        user.setEnabled(true);
        user.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        user.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        Mono<User> persisted = userRepository.save(user)
                .flatMap(saved -> userRepository.findUserById(saved.getId()));

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