package com.bcm.cluster_manager.config.security;

import com.bcm.shared.config.permissions.Role;
import com.bcm.shared.model.database.User;
import com.bcm.shared.repository.UserMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.GroupMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final UserGroupRelationMapper userGroupRelationMapper;
    private final GroupMapper groupMapper;

    public CustomUserDetailsService(@Qualifier("userMapperCM") UserMapper userMapper,
                                    @Qualifier("userGroupRelationMapperCM") UserGroupRelationMapper userGroupRelationMapper,
                                    @Qualifier("groupMapperCM") GroupMapper groupMapper) {
        this.userMapper = userMapper;
        this.userGroupRelationMapper = userGroupRelationMapper;
        this.groupMapper = groupMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userMapper.findByName(username);
        if (u == null || !Boolean.TRUE.equals(u.isEnabled())) {
            throw new UsernameNotFoundException("User not found or disabled");
        }

        var relations = userGroupRelationMapper.findByUser(u.getId());

        // Map groups to Role enum
        var roles = relations.stream()
                .map(rel -> groupMapper.findById(rel.getGroupId()))
                .map(g -> {
                    try {
                        // Convert group name to Role enum (e.g., "ADMINISTRATORS" -> Role.ADMINISTRATORS)
                        return Role.valueOf(g.getName().toUpperCase().replace(' ', '_'));
                    } catch (IllegalArgumentException e) {
                        throw new UsernameNotFoundException("Invalid role for user: " + g.getName());
                    }
                })
                .collect(Collectors.toSet());

        // Get all authorities (roles + permissions) from the roles
        List<GrantedAuthority> authorities = roles.stream()
                .map(Role::getAuthorities)
                .flatMap(Collection::stream)
                .distinct() // Remove duplicates if user has multiple groups with overlapping permissions
                .collect(Collectors.toList());

        return new CustomUserDetails(
                u.getId(),
                u.getName(),
                u.getPasswordHash(),
                u.isEnabled(),
                u.getCreatedAt(),
                u.getUpdatedAt(),
                authorities,
                roles // Pass the roles so they can be accessed later
        );
    }
}

