package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.config.security.CustomUserDetails;
import com.bcm.shared.config.permissions.Role;
import com.bcm.shared.model.database.User;
import com.bcm.shared.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CMUserControllerTest {

    @Mock
    private UserService userService;

    private CMUserController controller;

    @BeforeEach
    void setUp() {
        controller = new CMUserController(userService);
    }

    @Test
    void getUserBySubtext_shouldComputeRankAndPassToService() {

        Set<Role> roles = Set.of(Role.OPERATORS, Role.SUPERUSER);
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_SUPERUSER"));

        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "u", "pw", true,
                Instant.now(), Instant.now(),
                authorities, roles
        );

        SecurityContext ctx = new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities)
        );

        when(userService.getUserBySubtextWithRankCheck(eq("bob"), anyInt()))
                .thenReturn(Mono.just(List.of(new User())));

        StepVerifier.create(
                        controller.getUserBySubtext("bob")
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(ctx)))
                )
                .assertNext(list -> assertEquals(1, list.size()))
                .verifyComplete();

        ArgumentCaptor<Integer> rankCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(userService).getUserBySubtextWithRankCheck(eq("bob"), rankCaptor.capture());
        assertEquals(Role.SUPERUSER.getRank(), rankCaptor.getValue());
    }

    @Test
    void createUser_shouldSetTimestamps_andPassRankAndGroupId() {
        Set<Role> roles = Set.of(Role.ADMINISTRATORS);
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRATORS"));

        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "admin", "pw", true,
                Instant.now(), Instant.now(),
                authorities, roles
        );

        SecurityContext ctx = new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities)
        );

        User input = new User();
        input.setName("newuser");

        when(userService.addUserAndAssignGroupWithRankCheck(any(User.class), eq(5L), anyInt()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0, User.class)));

        StepVerifier.create(
                        controller.createUser(5L, input)
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(ctx)))
                )
                .assertNext(u -> {
                    assertNotNull(u.getCreatedAt());
                    assertNotNull(u.getUpdatedAt());
                    assertEquals("newuser", u.getName());
                })
                .verifyComplete();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Integer> rankCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(userService).addUserAndAssignGroupWithRankCheck(userCaptor.capture(), eq(5L), rankCaptor.capture());
        assertEquals(Role.ADMINISTRATORS.getRank(), rankCaptor.getValue());
        assertNotNull(userCaptor.getValue().getCreatedAt());
        assertNotNull(userCaptor.getValue().getUpdatedAt());
    }

    @Test
    void updateUser_shouldSetId_updatedAt_andNullPasswordHash_thenPassRank() {
        Set<Role> roles = Set.of(Role.OPERATORS);
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_OPERATORS"));

        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "op", "pw", true,
                Instant.now(), Instant.now(),
                authorities, roles
        );

        SecurityContext ctx = new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities)
        );

        User incoming = new User();
        incoming.setName("x");
        incoming.setPasswordHash("shouldBeCleared");

        when(userService.editUserWithRankCheck(any(User.class), anyInt()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0, User.class)));

        StepVerifier.create(
                        controller.updateUser(77L, incoming)
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(ctx)))
                )
                .assertNext(u -> {
                    assertEquals(77L, u.getId());
                    assertNotNull(u.getUpdatedAt());
                    assertNull(u.getPasswordHash());
                })
                .verifyComplete();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Integer> rankCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(userService).editUserWithRankCheck(userCaptor.capture(), rankCaptor.capture());
        assertEquals(77L, userCaptor.getValue().getId());
        assertNull(userCaptor.getValue().getPasswordHash());
        assertEquals(Role.OPERATORS.getRank(), rankCaptor.getValue());
    }
}
