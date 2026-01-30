package com.bcm.cluster_manager.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the cluster manager profile: CORS, security filter chain, and authentication.
 */
@Configuration
@EnableWebFluxSecurity
@Profile("cluster_manager")
public class SecurityConfig {

    /** Enables method-level security when application.security.enabled is true. */
    @Configuration
    @EnableReactiveMethodSecurity
    @ConditionalOnProperty(name = "application.security.enabled", havingValue = "true", matchIfMissing = false)
    public static class MethodSecurityConfig {
    }

    @Value("${application.cors.allowed-origin:http://localhost:4200}")
    private String allowedOrigin;

    /**
     * Provides the security context repository (web session–backed).
     *
     * @return the server security context repository
     */
    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    /**
     * Configures CORS to allow the configured frontend origin and common HTTP methods.
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Filter chain when security is disabled: CORS only, all paths permitted.
     *
     * @param http                    server HTTP security
     * @param corsConfigurationSource CORS configuration
     * @return the security web filter chain
     */
    @Bean
    @ConditionalOnProperty(name = "application.security.enabled", havingValue = "false", matchIfMissing = true)
    public SecurityWebFilterChain noSecurityFilterChain(ServerHttpSecurity http, CorsConfigurationSource corsConfigurationSource) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth.anyExchange().permitAll())
                .build();
    }

    /**
     * Filter chain when security is enabled: auth required except for login, logout, actuator, and BN endpoints.
     *
     * @param http                       server HTTP security
     * @param securityContextRepository  security context repository
     * @param corsConfigurationSource    CORS configuration
     * @return the security web filter chain
     */
    @Bean
    @ConditionalOnProperty(name = "application.security.enabled", havingValue = "true", matchIfMissing = false)
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, ServerSecurityContextRepository securityContextRepository, CorsConfigurationSource corsConfigurationSource) {
        return http
                .securityContextRepository(securityContextRepository)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers(
                                "/api/v1/cm/auth/login",
                                "/api/v1/cm/auth/logout"
                        ).permitAll()
                        .pathMatchers(
                                "/api/v1/ping",
                                "/api/v1/sync",
                                "/api/v1/example",
                                "/api/v1/cm/register",
                                "/api/v1/bn/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }

    /**
     * Reactive authentication manager using the user details service and password encoder.
     *
     * @param userDetailsService user details service
     * @param passwordEncoder    password encoder
     * @return the reactive authentication manager
     */
    @Bean
    public ReactiveAuthenticationManager authenticationManager(
            ReactiveUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder);
        return manager;
    }
}

