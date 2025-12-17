package com.bcm.shared.config.permissions;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void getAuthorities_shouldIncludeAllPermissionsAndRoleName_forSuperuser() {
        // Act
        List<SimpleGrantedAuthority> authorities = Role.SUPERUSER.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.size() > 0);

        // Check that all permissions are included
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("user:read")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("user:create")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("user:update")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("user:delete")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("node:read")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("node:create")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("node:delete")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("backup:read")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("backup:create")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("backup:delete")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("task:read")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("task:create")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("task:delete")));

        // Check that the role name with ROLE_ prefix is included
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERUSER")));
    }

    @Test
    void getAuthorities_shouldReturnUniqueAuthorities() {
        // Act
        List<SimpleGrantedAuthority> authorities = Role.SUPERUSER.getAuthorities();

        // Assert
        long uniqueCount = authorities.stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .distinct()
                .count();

        assertEquals(authorities.size(), uniqueCount, "Authorities should not contain duplicates");
    }

    @Test
    void getAuthorities_shouldAlwaysIncludeRolePrefix() {
        // Test all roles
        for (Role role : Role.values()) {
            List<SimpleGrantedAuthority> authorities = role.getAuthorities();

            assertTrue(
                authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name())),
                "Role " + role.name() + " should include ROLE_ prefixed authority"
            );
        }
    }

    @Test
    void getAuthorities_shouldReturnNewListInstance() {
        // Act
        List<SimpleGrantedAuthority> authorities1 = Role.SUPERUSER.getAuthorities();
        List<SimpleGrantedAuthority> authorities2 = Role.SUPERUSER.getAuthorities();

        // Assert - Should be different instances but equal content
        assertNotSame(authorities1, authorities2, "Should return new list instance each time");
        assertEquals(authorities1.size(), authorities2.size(), "Should have same size");
    }
}

