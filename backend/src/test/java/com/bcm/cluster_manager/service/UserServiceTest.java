package com.bcm.cluster_manager.service;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.model.database.UserGroupRelation;
import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.UserMapper;
import com.bcm.shared.model.database.User;
import com.bcm.shared.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserGroupRelationMapper userGroupRelationMapper;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        // Manually create UserService with constructor-injected mocks
        userService = new UserService(userGroupRelationMapper, userMapper, groupMapper);
        // Inject passwordEncoder since it's @Autowired on the field, not constructor-injected
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
    }

    @Test
    void addUserAndAssignGroup_insertsUser_andCreatesRelation() {
        User newUser = new User();
        newUser.setName("Alice");
        newUser.setPasswordHash("plainPassword");
        Long groupId = 10L;

        User persistedUser = new User();
        persistedUser.setId(42L);
        persistedUser.setName("Alice");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userMapper.save(any(User.class))).thenReturn(Mono.just(persistedUser));
        when(userGroupRelationMapper.save(any(UserGroupRelation.class)))
                .thenReturn(Mono.just(new UserGroupRelation()));
        when(userMapper.findUserById(42L)).thenReturn(Mono.just(persistedUser));

        StepVerifier.create(userService.addUserAndAssignGroup(newUser, groupId))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(42L, result.getId());
                    assertEquals("Alice", result.getName());
                })
                .verifyComplete();

        verify(userMapper).save(any(User.class));
        verify(userGroupRelationMapper).save(any(UserGroupRelation.class));
    }

    @Test
    void getUserBySubtextWithRankCheck_shouldReturnOnlyUsersWithLowerRank() {
        int requesterRank = 50;
        String searchText = "user";

        User user1 = new User();
        user1.setId(1L);
        user1.setName("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("user2");

        User user3 = new User();
        user3.setId(3L);
        user3.setName("user3");

        Group operatorGroup = new Group();
        operatorGroup.setId(1L);
        operatorGroup.setName("OPERATORS");
        operatorGroup.setEnabled(true);

        Group superuserGroup = new Group();
        superuserGroup.setId(2L);
        superuserGroup.setName("SUPERUSER");
        superuserGroup.setEnabled(true);

        UserGroupRelation relation1 = new UserGroupRelation();
        relation1.setUserId(1L);
        relation1.setGroupId(1L);

        UserGroupRelation relation2 = new UserGroupRelation();
        relation2.setUserId(2L);
        relation2.setGroupId(2L);

        UserGroupRelation relation3 = new UserGroupRelation();
        relation3.setUserId(3L);
        relation3.setGroupId(1L);

        when(userMapper.findByNameSubtext(searchText)).thenReturn(Flux.just(user1, user2, user3));
        when(userGroupRelationMapper.findByUser(1L)).thenReturn(Flux.just(relation1));
        when(userGroupRelationMapper.findByUser(2L)).thenReturn(Flux.just(relation2));
        when(userGroupRelationMapper.findByUser(3L)).thenReturn(Flux.just(relation3));
        when(groupMapper.findAllById(anyList())).thenAnswer(invocation -> {
            List<Long> idList = invocation.getArgument(0);
            if (idList.size() == 1 && idList.getFirst().equals(1L)) {
                return Flux.just(operatorGroup);
            } else if (idList.size() == 1 && idList.getFirst().equals(2L)) {
                return Flux.just(superuserGroup);
            } else {
                return Flux.just(operatorGroup, superuserGroup);
            }
        });

        StepVerifier.create(userService.getUserBySubtextWithRankCheck(searchText, requesterRank))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(2, result.size());
                    assertTrue(result.stream().anyMatch(u -> u.getId() == 1L));
                    assertTrue(result.stream().anyMatch(u -> u.getId() == 3L));
                    assertFalse(result.stream().anyMatch(u -> u.getId() == 2L));
                })
                .verifyComplete();
    }

    @Test
    void addUserAndAssignGroupWithRankCheck_shouldSucceed_whenRequesterHasHigherRank() {
        int requesterRank = 50;
        Long groupId = 1L;

        User newUser = new User();
        newUser.setName("newuser");
        newUser.setPasswordHash("password");

        User persistedUser = new User();
        persistedUser.setId(1L);
        persistedUser.setName("newuser");

        Group operatorGroup = new Group();
        operatorGroup.setId(1L);
        operatorGroup.setName("OPERATORS");
        operatorGroup.setEnabled(true);

        when(groupMapper.findById(groupId)).thenReturn(Mono.just(operatorGroup));
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userMapper.save(any(User.class))).thenReturn(Mono.just(persistedUser));
        when(userGroupRelationMapper.save(any(UserGroupRelation.class)))
                .thenReturn(Mono.just(new UserGroupRelation()));
        when(userMapper.findUserById(1L)).thenReturn(Mono.just(persistedUser));

        StepVerifier.create(userService.addUserAndAssignGroupWithRankCheck(newUser, groupId, requesterRank))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("newuser", result.getName());
                })
                .verifyComplete();

        verify(userMapper).save(any(User.class));
        verify(userGroupRelationMapper).save(any(UserGroupRelation.class));
    }

    @Test
    void addUserAndAssignGroupWithRankCheck_shouldThrowException_whenRequesterHasLowerRank() {
        int requesterRank = 1;
        Long groupId = 1L;

        User newUser = new User();
        newUser.setName("newuser");
        newUser.setPasswordHash("password");

        Group adminGroup = new Group();
        adminGroup.setId(1L);
        adminGroup.setName("ADMINISTRATORS");
        adminGroup.setEnabled(true);

        when(groupMapper.findById(groupId)).thenReturn(Mono.just(adminGroup));
        lenient().when(userMapper.save(any(User.class))).thenReturn(Mono.just(newUser));
        lenient().when(userMapper.findUserById(anyLong())).thenReturn(Mono.just(newUser));
        lenient().when(userGroupRelationMapper.save(any(UserGroupRelation.class))).thenReturn(Mono.just(new UserGroupRelation()));

        StepVerifier.create(userService.addUserAndAssignGroupWithRankCheck(newUser, groupId, requesterRank))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    void editUserWithRankCheck_shouldSucceed_whenRequesterHasHigherRank() {
        int requesterRank = 50;

        User user = new User();
        user.setId(1L);
        user.setName("updateduser");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("updateduser");

        Group operatorGroup = new Group();
        operatorGroup.setId(1L);
        operatorGroup.setName("OPERATORS");
        operatorGroup.setEnabled(true);

        UserGroupRelation relation = new UserGroupRelation();
        relation.setUserId(1L);
        relation.setGroupId(1L);

        when(userGroupRelationMapper.findByUser(1L)).thenReturn(Flux.just(relation));
        when(groupMapper.findAllById(List.of(1L))).thenReturn(Flux.just(operatorGroup));
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("oldname");
        when(userMapper.findUserById(1L))
                .thenReturn(Mono.just(existingUser))
                .thenReturn(Mono.just(updatedUser));
        when(userMapper.updateUser(anyLong(), anyString(), anyBoolean(), any())).thenReturn(Mono.empty());

        StepVerifier.create(userService.editUserWithRankCheck(user, requesterRank))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("updateduser", result.getName());
                })
                .verifyComplete();

        verify(userMapper).updateUser(anyLong(), anyString(), anyBoolean(), any());
    }

    @Test
    void addUserAndAssignGroupWithRankCheck_shouldThrowException_whenRequesterHasEqualRank() {
        int requesterRank = 50;
        Long groupId = 1L;

        User newUser = new User();
        newUser.setName("newuser");
        newUser.setPasswordHash("password");

        Group adminGroup = new Group();
        adminGroup.setId(1L);
        adminGroup.setName("ADMINISTRATORS");
        adminGroup.setEnabled(true);

        when(groupMapper.findById(groupId)).thenReturn(Mono.just(adminGroup));
        lenient().when(userMapper.save(any(User.class))).thenReturn(Mono.just(newUser));
        lenient().when(userMapper.findUserById(anyLong())).thenReturn(Mono.just(newUser));
        lenient().when(userGroupRelationMapper.save(any(UserGroupRelation.class))).thenReturn(Mono.just(new UserGroupRelation()));

        StepVerifier.create(userService.addUserAndAssignGroupWithRankCheck(newUser, groupId, requesterRank))
                .expectError(AccessDeniedException.class)
                .verify();
    }


    @Test
    void editUserWithRankCheck_shouldThrowException_whenRequesterHasLowerRank() {
        // Arrange - Requester has rank 1
        int requesterRank = 1;

        User user = new User();
        user.setId(1L);
        user.setName("updateduser");

        Group adminGroup = new Group();
        adminGroup.setId(1L);
        adminGroup.setName("ADMINISTRATORS");
        adminGroup.setEnabled(true);

        UserGroupRelation relation = new UserGroupRelation();
        relation.setUserId(1L);
        relation.setGroupId(1L);

        when(userGroupRelationMapper.findByUser(1L)).thenReturn(Flux.just(relation));
        when(groupMapper.findAllById(anyList())).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(0);
            if (ids.size() == 1 && ids.getFirst().equals(1L)) {
                return Flux.just(adminGroup);
            }
            return Flux.empty();
        });
        lenient().when(userMapper.findUserById(1L)).thenReturn(Mono.just(user));
        lenient().when(userMapper.updateUser(anyLong(), anyString(), anyBoolean(), any())).thenReturn(Mono.empty());
        lenient().when(userMapper.save(any(User.class))).thenReturn(Mono.just(user));

        StepVerifier.create(userService.editUserWithRankCheck(user, requesterRank))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(userMapper, never()).updateUser(anyLong(), anyString(), anyBoolean(), any());
        verify(userMapper, never()).save(any(User.class));
    }

    @Test
    void deleteUserWithRankCheck_shouldSucceed_whenRequesterHasHigherRank() {
        int requesterRank = 50;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Group operatorGroup = new Group();
        operatorGroup.setId(1L);
        operatorGroup.setName("OPERATORS");
        operatorGroup.setEnabled(true);

        UserGroupRelation relation = new UserGroupRelation();
        relation.setUserId(1L);
        relation.setGroupId(1L);

        when(userGroupRelationMapper.findByUser(userId)).thenReturn(Flux.just(relation));
        when(groupMapper.findAllById(List.of(1L))).thenReturn(Flux.just(operatorGroup));
        when(userGroupRelationMapper.deleteByUserId(userId)).thenReturn(Mono.empty());
        when(userMapper.existsUserById(userId)).thenReturn(Mono.just(true));
        when(userMapper.deleteUserById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUserWithRankCheck(userId, requesterRank))
                .assertNext(Assertions::assertTrue)
                .verifyComplete();

        verify(userMapper).deleteUserById(userId);
    }

    @Test
    void deleteUserWithRankCheck_shouldThrowException_whenRequesterHasLowerRank() {
        int requesterRank = 1;
        Long userId = 1L;

        Group adminGroup = new Group();
        adminGroup.setId(1L);
        adminGroup.setName("ADMINISTRATORS");
        adminGroup.setEnabled(true);

        UserGroupRelation relation = new UserGroupRelation();
        relation.setUserId(1L);
        relation.setGroupId(1L);

        when(userGroupRelationMapper.findByUser(userId)).thenReturn(Flux.just(relation));
        when(groupMapper.findAllById(List.of(1L))).thenReturn(Flux.just(adminGroup));
        lenient().when(userGroupRelationMapper.deleteByUserId(userId)).thenReturn(Mono.empty());
        lenient().when(userMapper.existsUserById(userId)).thenReturn(Mono.just(true));
        lenient().when(userMapper.deleteUserById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUserWithRankCheck(userId, requesterRank))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(userMapper, never()).deleteUserById(anyLong());
    }

    @Test
    void deleteUserWithRankCheck_shouldThrowException_whenRequesterHasEqualRank() {
        int requesterRank = 50;
        Long userId = 1L;

        Group adminGroup = new Group();
        adminGroup.setId(1L);
        adminGroup.setName("ADMINISTRATORS");
        adminGroup.setEnabled(true);

        UserGroupRelation relation = new UserGroupRelation();
        relation.setUserId(1L);
        relation.setGroupId(1L);

        when(userGroupRelationMapper.findByUser(userId)).thenReturn(Flux.just(relation));
        when(groupMapper.findAllById(List.of(1L))).thenReturn(Flux.just(adminGroup));
        lenient().when(userGroupRelationMapper.deleteByUserId(userId)).thenReturn(Mono.empty());
        lenient().when(userMapper.existsUserById(userId)).thenReturn(Mono.just(true));
        lenient().when(userMapper.deleteUserById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUserWithRankCheck(userId, requesterRank))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(userMapper, never()).deleteUserById(anyLong());
    }

}