package com.bcm.cluster_manager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@RestController
@RequestMapping("/api/v1/auth")
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
        try {
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
        } catch (AuthenticationException e) {
            // Bad credentials or user not found
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/check-auth")
    public ResponseEntity<Void> checkAuth(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            var context = (org.springframework.security.core.context.SecurityContext) session.getAttribute(SPRING_SECURITY_CONTEXT_KEY);
            if (context != null && context.getAuthentication() != null && context.getAuthentication().isAuthenticated()) {
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}

