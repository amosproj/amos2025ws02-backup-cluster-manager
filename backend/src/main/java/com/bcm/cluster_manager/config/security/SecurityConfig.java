package com.bcm.cluster_manager.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("cluster_manager")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configure(http))
                .csrf(AbstractHttpConfigurer::disable) // It would be possible to configure CSRF tokens for the FE
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/cm/auth/login",
                                "/api/v1/cm/auth/logout"
                        ).permitAll() // Only auth/login and auth/logout are accessible without authentication.
                        .requestMatchers(
                                "/api/v1/ping",
                                "/api/v1/sync",
                                "/api/v1/example",
                                "/api/v1/cm/register"
                        ).permitAll() // Node-to-node communication endpoints
                        // Here more requestMatchers can be added also with hasRole
                        // but without prefix ROLE_ as that is attached automatically by Spring security
                        .anyRequest().authenticated() // All other requests require authentication / can be turned off by using .permitAll()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

