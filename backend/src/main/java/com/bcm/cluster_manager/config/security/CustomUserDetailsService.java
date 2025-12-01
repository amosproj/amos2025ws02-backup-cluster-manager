package com.bcm.cluster_manager.config.security;

import com.bcm.cluster_manager.model.database.User;
import com.bcm.cluster_manager.repository.UserMapper;
import com.bcm.cluster_manager.repository.UserGroupRelationMapper;
import com.bcm.cluster_manager.repository.GroupMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final UserGroupRelationMapper userGroupRelationMapper;
    private final GroupMapper groupMapper;

    public CustomUserDetailsService(UserMapper userMapper,
                                    UserGroupRelationMapper userGroupRelationMapper,
                                    GroupMapper groupMapper) {
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

        // ROLE_ prefix is required by Spring Security!
        List<SimpleGrantedAuthority> authorities = relations.stream()
                .map(rel -> groupMapper.findById(rel.getGroupId()))
                .map(g -> new SimpleGrantedAuthority("ROLE_" + g.getName().toUpperCase().replace(' ', '_')))
                .collect(Collectors.toList());

        return new CustomUserDetails(
                u.getId(),
                u.getName(),
                u.getPasswordHash(),
                u.isEnabled(),
                u.getCreatedAt(),
                u.getUpdatedAt(),
                authorities
        );
    }
}

