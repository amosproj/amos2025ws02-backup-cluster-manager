package com.bcm.shared.repository;

import com.bcm.shared.model.database.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.ANY) // use in-memory DB for tests
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRepositoryTest {

    @Autowired
    private UserMapper userRepository;

    @BeforeAll
    void setup() {
        // Optional: ensure schema is present if not managed by migrations
    }

    @Test
    void testInsertLogsIntReturnAndPersists() {
        // Arrange: create a new user
        User user = new User();
        user.setName("testuser_" + System.currentTimeMillis());
        user.setPasswordHash("hashedpwd");
        user.setEnabled(true);
        user.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        user.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        // Act: insert and capture the int return value
        int rowsInserted = userRepository.insert(user);

        // Log the int return value (printing for demonstration; replace with a logger if preferred)
        System.out.println("Rows inserted: " + rowsInserted);

        // Assert: at least one row inserted
        assertTrue(rowsInserted > 0, "Insert should affect at least one row");

        // Optional: verify the object was persisted by fetching it back
        User persisted = userRepository.findById(1L);
        assertNotNull(persisted, "Persisted user should be retrievable");
        assertEquals(user.getName(), persisted.getName(), "Names should match");
        assertEquals(user.getPasswordHash(), persisted.getPasswordHash(), "Password hash should match");
        assertEquals(user.isEnabled(), persisted.isEnabled(), "Enabled flag should match");
    }

}

// Additional tests (update, delete, retrieval) can follow