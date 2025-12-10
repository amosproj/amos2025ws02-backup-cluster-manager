package com.bcm.cluster_manager.repository;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.model.database.User;
import com.bcm.shared.model.database.UserGroupRelation;
import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.UserMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.ANY) // Use in-memory DB for tests
@Disabled("Skipping Spring context startup for now")
class UserGroupRelationMapperTest {

    @Qualifier("userGroupRelationMapperBN")
    @Autowired
    private UserGroupRelationMapper userGroupRelationMapper;

    private UserGroupRelation createTestUserGroupRelation(Long userId, Long groupId) {
        UserGroupRelation ugr = new UserGroupRelation();
        ugr.setUserId(userId);
        ugr.setGroupId(groupId);
        ugr.setAddedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        userGroupRelationMapper.insert(ugr);

        return ugr;
    }

    @Autowired
    private UserMapper userMapper;

    private User createTestUser() {
        // Arrange: create a new user
        User user = new User();
        user.setName("testuser_" + System.currentTimeMillis());
        user.setPasswordHash("hashedpwd");
        user.setEnabled(true);
        user.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        user.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        // Act: insert and capture the int return value
        int rowsInserted = userMapper.insert(user);

        return user;
    }

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
    void testExistsWhenRelationExists() {
        User user = createTestUser();
        Group group = createTestGroup();
        UserGroupRelation ugr = createTestUserGroupRelation(user.getId(), group.getId());

        // Act
        boolean result = userGroupRelationMapper.exists(user.getId(), group.getId());

        // Assert
        assertTrue(result, "Exists should return true when the relation exists");
    }

    @Test
    void testExistsWhenRelationDoesNotExist() {
        // Arrange
        Long userId = 99L;
        Long groupId = 99L;

        // Act
        boolean result = userGroupRelationMapper.exists(userId, groupId);

        // Assert
        assertFalse(result, "Exists should return false when the relation does not exist");
    }
}