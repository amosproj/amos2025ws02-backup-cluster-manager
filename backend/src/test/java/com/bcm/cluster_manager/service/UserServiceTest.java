package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.repository.UserGroupRelationMapper;
import com.bcm.cluster_manager.repository.UserMapper;
import com.bcm.cluster_manager.model.database.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserGroupRelationMapper userGroupRelationMapper;

    @InjectMocks
    private UserService userService; // nimmt den Konstruktor und injiziert Mocks

    @Test
    void addUserAndAssignGroup_insertsUser_andCreatesRelation() {
        // Arrange
        User newUser = new User();
        newUser.setName("Alice");
        Long groupId = 10L;

        User persistedUser = new User();
        persistedUser.setId(42L);
        persistedUser.setName("Alice");

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
        Mockito.verify(userMapper).insert(any(User.class));

        // Relation mit passender User-ID und GroupID
        Mockito.verify(userGroupRelationMapper).insert(
                Mockito.argThat(rel ->
                        rel.getUserId() == 42L &&
                                rel.getGroupId().equals(groupId)
                ));
    }
}
