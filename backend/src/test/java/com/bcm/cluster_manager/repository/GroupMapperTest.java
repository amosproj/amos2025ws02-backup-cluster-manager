package com.bcm.cluster_manager.repository;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.repository.GroupMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for the GroupMapper class to ensure proper database access functionality.
 * These tests focus on validating the `findById` method, which retrieves a group by its ID.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Transactional
@Rollback
@Disabled("Skipping Spring context startup for now")
class GroupMapperTest {

    @Qualifier("groupMapperBN")
    @Autowired
    private GroupMapper groupMapper;

    /**
     * Creates and persists a new test group instance.
     * The group is initialized with a unique name, enabled status, and the current timestamp
     * for both creation and update times. The group is then inserted into the database.
     *
     * @return the created and persisted test group instance
     */
    private Group createTestGroup() {
        Group group = new Group();
        group.setName("Superuser");
        group.setEnabled(true);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        group.setCreatedAt(now);
        group.setUpdatedAt(now);
        groupMapper.insert(group);
        return group;
    }

    @Test
    void findById_shouldReturnGroupForValidId() {
        // Arrange
        Group createdGroup = createTestGroup();

        // Act
        Group foundGroup = groupMapper.findById(createdGroup.getId());

        // Assert
        assertThat(foundGroup).isNotNull();
        assertThat(foundGroup.getId()).isEqualTo(createdGroup.getId());
        assertThat(foundGroup.getName()).isEqualTo(createdGroup.getName());
        assertThat(foundGroup.isEnabled()).isEqualTo(createdGroup.isEnabled());
        assertThat(foundGroup.getCreatedAt()).isEqualTo(createdGroup.getCreatedAt());
        assertThat(foundGroup.getUpdatedAt()).isEqualTo(createdGroup.getUpdatedAt());
    }

    @Test
    void findById_shouldReturnNullForInvalidId() {
        // Act
        Group foundGroup = groupMapper.findById(-1L); // Non-existent ID

        // Assert
        assertThat(foundGroup).isNull();
    }
}