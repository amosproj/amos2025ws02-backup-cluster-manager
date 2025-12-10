package com.bcm.cluster_manager.service;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.model.database.UserGroupRelation;
import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.UserMapper;
import com.bcm.shared.model.database.User;
import com.bcm.shared.service.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled("Skipping Spring context startup for now")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService; // nimmt den Konstruktor und injiziert Mocks

    @Mock
    private UserGroupRelationMapper userGroupRelationMapper;

        // Inject passwordEncoder since it's @Autowired on the field, not constructor-injected
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
    }

    @Test
        // Arrange
        User newUser = new User();
        newUser.setName("Alice");
        newUser.setPasswordHash("plainPassword");
        Long groupId = 10L;


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
