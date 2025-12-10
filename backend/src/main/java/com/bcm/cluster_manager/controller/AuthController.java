package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.config.security.CustomUserDetails;
import com.bcm.shared.config.permissions.Role;
import com.bcm.shared.model.api.AuthMetadataDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Set;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@RestController
@RequestMapping("/api/v1/cm/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(req.username, req.password);

        Authentication auth = authenticationManager.authenticate(token);

        // You have to explicitly pass auth to context and context to session. As of this version, I guess.
        SecurityContextHolder.getContext().setAuthentication(auth);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<AuthMetadataDTO> validateSession() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Cast to CustomUserDetails (not User entity!)
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        // Get the first role (or handle multiple roles if needed)
        var roles = userDetails.getRoles();
        if (roles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
    }
}

