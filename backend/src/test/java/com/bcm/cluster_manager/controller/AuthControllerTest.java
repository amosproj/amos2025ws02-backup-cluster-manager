package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.config.security.CustomUserDetails;
import com.bcm.shared.config.permissions.Role;
import com.bcm.shared.model.api.AuthMetadataDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthController authController;

    @Test
    void validateSession_shouldReturnAuthMetadata_whenUserIsAuthenticated() {
        // Arrange
        Set<Role> roles = Set.of(Role.ADMINISTRATORS);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMINISTRATORS"));
        authorities.add(new SimpleGrantedAuthority("user:read"));
        authorities.add(new SimpleGrantedAuthority("user:create"));

        CustomUserDetails userDetails = new CustomUserDetails(
            1L,
            "testuser",
            "hashedPassword",
            true,
            Instant.now(),
            Instant.now(),
            authorities,
            roles
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Act
        Mono<ResponseEntity<AuthMetadataDTO>> mono = authController.validateSession();

        // Assert
        StepVerifier.create(mono)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());

                    AuthMetadataDTO metadata = response.getBody();
                    assertEquals("testuser", metadata.getUsername());
                    assertEquals(Role.ADMINISTRATORS, metadata.getRole());
                    assertEquals(50, metadata.getRank());
                    assertTrue(metadata.getPermissions().contains("user:read"));
                    assertTrue(metadata.getPermissions().contains("user:create"));
                })
                .verifyComplete();

        SecurityContextHolder.clearContext();
    }

    @Test
    void validateSession_shouldReturnUnauthorized_whenNoAuthentication() {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(securityContext);

        StepVerifier.create(authController.validateSession())
                .assertNext(response -> {
                    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
                    assertNull(response.getBody());
                })
                .verifyComplete();

        SecurityContextHolder.clearContext();
    }

    @Test
    void validateSession_shouldReturnUnauthorized_whenAuthenticationIsAnonymous() {
        // Arrange
        Authentication auth = new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Act + Assert
        StepVerifier.create(authController.validateSession())
                .assertNext(response -> {
                    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
                    assertNull(response.getBody());
                })
                .verifyComplete();

        SecurityContextHolder.clearContext();
    }

    @Test
    void validateSession_shouldReturnUnauthorized_whenAuthenticationIsNotAuthenticated() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Act + Assert
        StepVerifier.create(authController.validateSession())
                .assertNext(response -> {
                    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
                    assertNull(response.getBody());
                })
                .verifyComplete();

        SecurityContextHolder.clearContext();
    }

    @Test
    void validateSession_shouldReturnForbidden_whenUserHasNoRoles() {
        Set<Role> emptyRoles = Collections.emptySet();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "testuser", "hashedPassword", true,
                Instant.now(), Instant.now(),
                authorities, emptyRoles
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        StepVerifier.create(authController.validateSession())
                .assertNext(response -> {
                    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
                    assertNull(response.getBody());
                })
                .verifyComplete();

        SecurityContextHolder.clearContext();
    }


    @Test
    void validateSession_shouldReturnHighestRankedRole_whenUserHasMultipleRoles() {
        // Arrange
        Set<Role> roles = Set.of(Role.SUPERUSER, Role.OPERATORS);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_SUPERUSER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_OPERATORS"));
        authorities.add(new SimpleGrantedAuthority("user:read"));
        authorities.add(new SimpleGrantedAuthority("node:read"));

        CustomUserDetails userDetails = new CustomUserDetails(
                1L,
                "testuser",
                "hashedPassword",
                true,
                Instant.now(),
                Instant.now(),
                authorities,
                roles
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Act + Assert
        StepVerifier.create(authController.validateSession())
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());

                    AuthMetadataDTO metadata = response.getBody();
                    assertEquals(Role.SUPERUSER, metadata.getRole());
                    assertEquals(100, metadata.getRank());
                })
                .verifyComplete();

        SecurityContextHolder.clearContext();
    }
}

