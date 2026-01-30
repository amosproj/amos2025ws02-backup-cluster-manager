package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.config.security.CustomUserDetails;
import com.bcm.shared.config.permissions.Role;
import com.bcm.shared.model.api.AuthMetadataDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpSession;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

/**
 * REST controller for cluster manager authentication: login, logout, and session validation.
 */
@RestController
@RequestMapping("/api/v1/cm/auth")
public class AuthController {

//    private final AuthenticationManager authenticationManager;


    private final ReactiveAuthenticationManager authenticationManager;
    private final ServerSecurityContextRepository securityContextRepository;

    /**
     * Creates the auth controller with the given authentication manager and security context repository.
     *
     * @param authenticationManager      reactive authentication manager
     * @param securityContextRepository repository for storing security context in the session
     */
    public AuthController(ReactiveAuthenticationManager authenticationManager,
                          ServerSecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    /** Request body for login. */
    public static class LoginRequest {
        /** Username. */
        public String username;
        /** Password. */
        public String password;
    }

    /**
     * Authenticates the user and establishes a session.
     *
     * @param req     login credentials
     * @param exchange the current server web exchange
     * @return 200 on success, 401 on authentication failure
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<Void>> login(@RequestBody LoginRequest req, ServerWebExchange exchange) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(req.username, req.password);

        return authenticationManager.authenticate(token)
                .flatMap(auth -> {
                    SecurityContext context = new SecurityContextImpl(auth);
                    return securityContextRepository.save(exchange, context)
                            .thenReturn(ResponseEntity.ok().<Void>build());
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    /**
     * Invalidates the current session (logout).
     *
     * @param exchange the current server web exchange
     * @return 200 on success
     */
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {
        return exchange.getSession()
                .doOnNext(session -> session.getAttributes().remove(SPRING_SECURITY_CONTEXT_KEY))
                .thenReturn(ResponseEntity.ok().<Void>build());
    }

    /**
     * Validates the current session and returns the authenticated user's metadata (username, role, permissions).
     *
     * @return 200 with auth metadata if authenticated, 401 if not authenticated, 403 if no valid role
     */
    @GetMapping("/validate")
    public Mono<ResponseEntity<AuthMetadataDTO>> validateSession() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken))
                .map(auth -> {
                    CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

                    // Get the first role (or handle multiple roles if needed)
                    var roles = userDetails.getRoles();
                    if (roles.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<AuthMetadataDTO>build();
                    }

                    // Get the highest ranked role
                    Role primaryRole = roles.stream()
                            .max((r1, r2) -> Integer.compare(r1.getRank(), r2.getRank()))
                            .orElseThrow();

                    // Collect all unique permissions from all roles
                    Set<String> permissions = roles.stream()
                            .flatMap(role -> role.getPermissions().stream())
                            .map(com.bcm.shared.config.permissions.Permission::getPermission)
                            .collect(java.util.stream.Collectors.toSet());

                    AuthMetadataDTO metaData = new AuthMetadataDTO(
                            userDetails.getUsername(),
                            primaryRole,
                            primaryRole.getRank(),
                            permissions
                    );

                    return ResponseEntity.ok(metaData);
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
