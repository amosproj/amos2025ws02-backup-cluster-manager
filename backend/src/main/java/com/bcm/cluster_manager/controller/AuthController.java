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

@RestController
@RequestMapping("/api/v1/cm/auth")
public class AuthController {

//    private final AuthenticationManager authenticationManager;


    private final ReactiveAuthenticationManager authenticationManager;
    private final ServerSecurityContextRepository securityContextRepository;

    public AuthController(ReactiveAuthenticationManager authenticationManager,
                          ServerSecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

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

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> {
                    session.invalidate();
                    return Mono.just(ResponseEntity.ok().<Void>build());
                });
    }

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

