package com.bcm.cluster_manager.repository;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.model.database.User;
import com.bcm.shared.model.database.UserGroupRelation;
import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserGroupRelationMapperTest extends AbstractBnDbTest {

    @Autowired
    private UserGroupRelationMapper userGroupRelationMapper;

    @Autowired
    private UserMapper userMapper;

    private Mono<User> createTestUser() {
        User user = new User();
        user.setName("testuser_" + System.currentTimeMillis());
        user.setPasswordHash("hashedpwd");
        user.setEnabled(true);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        return userMapper.save(user)
                .doOnNext(saved -> assertThat(saved.getId()).isNotNull());
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
    private Mono<Group> createTestGroup() {
        Group group = new Group();
        group.setName("Superuser");
        group.setEnabled(true);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        group.setCreatedAt(now);
        group.setUpdatedAt(now);

        return groupMapper.save(group)
                .doOnNext(saved -> assertThat(saved.getId()).isNotNull());
    }

    @Test
    void testExistsWhenRelationExists() {
        Mono<Boolean> flow =
                createTestUser()
                        .zipWith(createTestGroup())
                        .flatMap(tuple -> {
                            User user = tuple.getT1();
                            Group group = tuple.getT2();

                            return userGroupRelationMapper.insert(createUgr(user.getId(), group.getId()))
                                    .then(userGroupRelationMapper.exists(user.getId(), group.getId()));
                        });

        StepVerifier.create(flow)
                .expectNext(true)
                .verifyComplete();
    }

    private UserGroupRelation createUgr(Long userId, Long groupId) {
        UserGroupRelation ugr = new UserGroupRelation();
        ugr.setUserId(userId);
        ugr.setGroupId(groupId);
        ugr.setAddedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        return ugr;
    }

    @Test
    void testExistsWhenRelationDoesNotExist() {
        Mono<Boolean> flow = userGroupRelationMapper.exists(Long.MAX_VALUE - 1, Long.MAX_VALUE - 1);

        StepVerifier.create(flow)
                .expectNext(false)
                .verifyComplete();
    }
}