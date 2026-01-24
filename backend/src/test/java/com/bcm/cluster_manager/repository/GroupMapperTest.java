package com.bcm.cluster_manager.repository;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.repository.GroupMapper;
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
 * Test suite for the GroupMapper class to ensure proper database access functionality.
 * These tests focus on validating the `findById` method, which retrieves a group by its ID.
 */
@SpringBootTest
@ActiveProfiles("test")
class GroupMapperTest extends AbstractBnDbTest {

    @Autowired
    private GroupMapper groupMapper;

    /**
     * Creates and persists a new test group instance.
     * The group is initialized with a unique name, enabled status, and the current timestamp
     * for both creation and update times. The group is then inserted into the database.
     *
     * @return the created and persisted test group instance
     */
    private Mono<Group> createTestGroup() {
        Group group = new Group();
        group.setName("Superuser");
        group.setEnabled(true);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        group.setCreatedAt(now);
        group.setUpdatedAt(now);
        return groupMapper.save(group);
    }

    @Test
    void findById_shouldReturnGroupForValidId() {
        Mono<Group> flow = createTestGroup()
                .flatMap(group -> groupMapper.findById(group.getId()));
        StepVerifier.create(flow)
                .assertNext(arr -> {
                    assertThat(arr).isNotNull();
                    assertThat(arr.getName()).isEqualTo("Superuser");
                    assertThat(arr.isEnabled()).isTrue();
                    assertThat(arr.getCreatedAt()).isNotNull();
                }).verifyComplete();
    }

    @Test
    void findById_shouldReturnNullForInvalidId() {
        StepVerifier.create(groupMapper.findById(-1L))
                .verifyComplete();
    }
}