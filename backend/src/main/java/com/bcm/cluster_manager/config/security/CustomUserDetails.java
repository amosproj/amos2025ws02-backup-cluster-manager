package com.bcm.cluster_manager.config.security;

import com.bcm.shared.config.permissions.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

/**
 * Spring Security user details implementation holding user id, username, password, roles, and authorities.
 */
public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Set<Role> roles; // Store the original roles

    public CustomUserDetails(
            Long id,
            String username,
            String passwordHash,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt,
            Collection<? extends GrantedAuthority> authorities,
            Set<Role> roles
    ) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.authorities = authorities;
        this.roles = roles;
    }

    /** Returns the user id. */
    public Long getId() { return id; }
    /** Returns the user's roles. */
    public Set<Role> getRoles() { return roles; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
