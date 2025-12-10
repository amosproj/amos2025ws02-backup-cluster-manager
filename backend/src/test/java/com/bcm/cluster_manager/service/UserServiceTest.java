package com.bcm.cluster_manager.service;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.model.database.UserGroupRelation;
import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.UserMapper;
import com.bcm.shared.model.database.User;
import com.bcm.shared.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        // Arrange
        User newUser = new User();
        newUser.setName("Alice");
        newUser.setPasswordHash("plainPassword");
        Long groupId = 10L;

        User persistedUser = new User();
        persistedUser.setId(42L);
        persistedUser.setName("Alice");

        // Mock password encoder
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        // Simuliere: insert setzt die ID des Users
        when(userMapper.insert(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(42L);
            return 1; // affected rows
        });

        when(userMapper.findById(42L)).thenReturn(persistedUser);

        // wenn dein Relation-Mapper z.B. int zurückgibt:
        when(userGroupRelationMapper.insert(
                Mockito.argThat(rel ->
                        rel.getUserId() == 42L &&
                                rel.getGroupId().equals(groupId)
                ))).thenReturn(1);

        // Act
        User result = userService.addUserAndAssignGroup(newUser, groupId);

        // Assert – Ergebnis
        assertNotNull(result);
        assertEquals(42L, result.getId());
        assertEquals("Alice", result.getName());

        // Assert – Interaktionen
        // 1x User gespeichert
        verify(userMapper).insert(any(User.class));

        // Relation mit passender User-ID und GroupID
        verify(userGroupRelationMapper).insert(
                Mockito.argThat(rel ->
                        rel.getUserId() == 42L &&
                                rel.getGroupId().equals(groupId)
                ));
    }

    @Test
    void getUserBySubtextWithRankCheck_shouldReturnOnlyUsersWithLowerRank() {
        // Arrange - Requester has rank 50 (ADMINISTRATORS)
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

        // Mock findByNameSubtext
        when(userMapper.findByNameSubtext(searchText)).thenReturn(Arrays.asList(user1, user2, user3));

        // Mock user ranks: user1 = 1, user2 = 100, user3 = 1
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

        when(userGroupRelationMapper.findByUser(1L)).thenReturn(List.of(relation1));
        when(userGroupRelationMapper.findByUser(2L)).thenReturn(List.of(relation2));
        when(userGroupRelationMapper.findByUser(3L)).thenReturn(List.of(relation3));

        when(groupMapper.findById(1L)).thenReturn(operatorGroup);
        when(groupMapper.findById(2L)).thenReturn(superuserGroup);

        // Act
        List<User> result = userService.getUserBySubtextWithRankCheck(searchText, requesterRank);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getId() == 1L));
        assertTrue(result.stream().anyMatch(u -> u.getId() == 3L));
        assertFalse(result.stream().anyMatch(u -> u.getId() == 2L)); // Superuser should be filtered out
    }

    @Test
    void getUserBySubtextWithRankCheck_shouldReturnEmptyList_whenAllUsersHaveHigherOrEqualRank() {
        // Arrange - Requester has rank 1
        int requesterRank = 1;
        String searchText = "user";

        User user1 = new User();
        user1.setId(1L);
        user1.setName("user1");

        when(userMapper.findByNameSubtext(searchText)).thenReturn(List.of(user1));

        Group operatorGroup = new Group();
        operatorGroup.setId(1L);
        operatorGroup.setName("OPERATORS");
        operatorGroup.setEnabled(true);

        UserGroupRelation relation1 = new UserGroupRelation();
        relation1.setUserId(1L);
        relation1.setGroupId(1L);

        when(userGroupRelationMapper.findByUser(1L)).thenReturn(List.of(relation1));
        when(groupMapper.findById(1L)).thenReturn(operatorGroup);

        // Act
        List<User> result = userService.getUserBySubtextWithRankCheck(searchText, requesterRank);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void addUserAndAssignGroupWithRankCheck_shouldSucceed_whenRequesterHasHigherRank() {
        // Arrange - Requester has rank 50 (ADMINISTRATORS)
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

        when(groupMapper.findById(groupId)).thenReturn(operatorGroup);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return 1;
        });
        when(userMapper.findById(1L)).thenReturn(persistedUser);
        when(userGroupRelationMapper.insert(any(UserGroupRelation.class))).thenReturn(1);

        // Act
        User result = userService.addUserAndAssignGroupWithRankCheck(newUser, groupId, requesterRank);

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getName());
        verify(userMapper).insert(any(User.class));
        verify(userGroupRelationMapper).insert(any(UserGroupRelation.class));
    }

    @Test
    void addUserAndAssignGroupWithRankCheck_shouldThrowException_whenRequesterHasLowerRank() {
        // Arrange - Requester has rank 1
        int requesterRank = 1;
        Long groupId = 1L;

        User newUser = new User();
        newUser.setName("newuser");
        newUser.setPasswordHash("password");

        Group adminGroup = new Group();
        adminGroup.setId(1L);
        adminGroup.setName("ADMINISTRATORS");
        adminGroup.setEnabled(true);

        when(groupMapper.findById(groupId)).thenReturn(adminGroup);

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> userService.addUserAndAssignGroupWithRankCheck(newUser, groupId, requesterRank));

        verify(userMapper, never()).insert(any(User.class));
        verify(userGroupRelationMapper, never()).insert(any(UserGroupRelation.class));
    }

    @Test
    void addUserAndAssignGroupWithRankCheck_shouldThrowException_whenRequesterHasEqualRank() {
        // Arrange - Requester has rank 50
        int requesterRank = 50;
        Long groupId = 1L;

        User newUser = new User();
        newUser.setName("newuser");
        newUser.setPasswordHash("password");

        Group adminGroup = new Group();
        adminGroup.setId(1L);
        adminGroup.setName("ADMINISTRATORS");
        adminGroup.setEnabled(true);

        when(groupMapper.findById(groupId)).thenReturn(adminGroup);

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> userService.addUserAndAssignGroupWithRankCheck(newUser, groupId, requesterRank));

        verify(userMapper, never()).insert(any(User.class));
        verify(userGroupRelationMapper, never()).insert(any(UserGroupRelation.class));
    }

    @Test
    void editUserWithRankCheck_shouldSucceed_whenRequesterHasHigherRank() {
        // Arrange - Requester has rank 50
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

        when(userGroupRelationMapper.findByUser(1L)).thenReturn(List.of(relation));
        when(groupMapper.findById(1L)).thenReturn(operatorGroup);
        when(userMapper.update(user)).thenReturn(1);
        when(userMapper.findById(1L)).thenReturn(updatedUser);

        // Act
        User result = userService.editUserWithRankCheck(user, requesterRank);

        // Assert
        assertNotNull(result);
        assertEquals("updateduser", result.getName());
        verify(userMapper).update(user);
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

        when(userGroupRelationMapper.findByUser(1L)).thenReturn(List.of(relation));
        when(groupMapper.findById(1L)).thenReturn(adminGroup);

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> userService.editUserWithRankCheck(user, requesterRank));

        verify(userMapper, never()).update(any(User.class));
    }

    @Test
    void deleteUserWithRankCheck_shouldSucceed_whenRequesterHasHigherRank() {
        // Arrange - Requester has rank 50
        int requesterRank = 50;
        Long userId = 1L;

        Group operatorGroup = new Group();
        operatorGroup.setId(1L);
        operatorGroup.setName("OPERATORS");
        operatorGroup.setEnabled(true);

        UserGroupRelation relation = new UserGroupRelation();
        relation.setUserId(1L);
        relation.setGroupId(1L);

        when(userGroupRelationMapper.findByUser(userId)).thenReturn(List.of(relation));
        when(groupMapper.findById(1L)).thenReturn(operatorGroup);
        when(userMapper.delete(userId)).thenReturn(1);

        // Act
        boolean result = userService.deleteUserWithRankCheck(userId, requesterRank);

        // Assert
        assertTrue(result);
        verify(userMapper).delete(userId);
    }

    @Test
    void deleteUserWithRankCheck_shouldThrowException_whenRequesterHasLowerRank() {
        // Arrange - Requester has rank 1
        int requesterRank = 1;
        Long userId = 1L;

        Group adminGroup = new Group();
        adminGroup.setId(1L);
        adminGroup.setName("ADMINISTRATORS");
        adminGroup.setEnabled(true);

        UserGroupRelation relation = new UserGroupRelation();
        relation.setUserId(1L);
        relation.setGroupId(1L);

        when(userGroupRelationMapper.findByUser(userId)).thenReturn(List.of(relation));
        when(groupMapper.findById(1L)).thenReturn(adminGroup);

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> userService.deleteUserWithRankCheck(userId, requesterRank));

        verify(userMapper, never()).delete(anyLong());
    }

    @Test
    void deleteUserWithRankCheck_shouldThrowException_whenRequesterHasEqualRank() {
        // Arrange - Requester has rank 50
        int requesterRank = 50;
        Long userId = 1L;

        Group adminGroup = new Group();
        adminGroup.setId(1L);
        adminGroup.setName("ADMINISTRATORS");
        adminGroup.setEnabled(true);

        UserGroupRelation relation = new UserGroupRelation();
        relation.setUserId(1L);
        relation.setGroupId(1L);

        when(userGroupRelationMapper.findByUser(userId)).thenReturn(List.of(relation));
        when(groupMapper.findById(1L)).thenReturn(adminGroup);

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> userService.deleteUserWithRankCheck(userId, requesterRank));

        verify(userMapper, never()).delete(anyLong());
    }
}