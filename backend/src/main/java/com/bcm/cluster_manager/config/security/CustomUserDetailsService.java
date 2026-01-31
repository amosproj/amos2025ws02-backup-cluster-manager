package com.bcm.cluster_manager.config.security;

import com.bcm.shared.config.permissions.Role;
import com.bcm.shared.model.database.Group;
import com.bcm.shared.model.database.User;
import com.bcm.shared.model.database.UserGroupRelation;
import com.bcm.shared.repository.UserMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.GroupMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loads user details by username for Spring Security (cluster manager); resolves roles from groups.
 */
@Service
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserMapper userMapper;
    private final UserGroupRelationMapper userGroupRelationMapper;
    private final GroupMapper groupMapper;

    /**
     * Creates the user details service with the given mappers.
     *
     * @param userMapper                 user mapper
     * @param userGroupRelationMapper   user-group relation mapper
     * @param groupMapper               group mapper
     */
    public CustomUserDetailsService(UserMapper userMapper,
                                     UserGroupRelationMapper userGroupRelationMapper,
                                     GroupMapper groupMapper) {
        this.userMapper = userMapper;
        this.userGroupRelationMapper = userGroupRelationMapper;
        this.groupMapper = groupMapper;
    }

    /**
     * Loads user details by username; returns enabled user with roles and authorities from groups.
     *
     * @param username username
     * @return user details, or error if not found or disabled
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userMapper.findByName(username)              // Mono<User>
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found or disabled")))
                .flatMap(u -> {
                    if (!Boolean.TRUE.equals(u.isEnabled())) {
                        return Mono.error(new UsernameNotFoundException("User not found or disabled"));
                    }

                    return userGroupRelationMapper.findByUser(u.getId())     // Flux<UserGroupRelation>
                            .map(UserGroupRelation::getGroupId)
                            .distinct()
                            .collectList()
                            .flatMapMany(groupMapper::findAllById)               // Flux<Group> (no N+1)
                            .map(g -> {
                                try {
                                    return Role.valueOf(g.getName().toUpperCase().replace(' ', '_'));
                                } catch (IllegalArgumentException e) {
                                    throw new UsernameNotFoundException("Invalid role for user: " + g.getName());
                                }
                            })
                            .collectList()
                            .map(roleList -> {
                                Set<Role> roles = Set.copyOf(roleList);

                                List<GrantedAuthority> authorities = roles.stream()
                                        .map(Role::getAuthorities)
                                        .flatMap(Collection::stream)
                                        .distinct()
                                        .collect(Collectors.toList());

                                return (UserDetails) new CustomUserDetails(
                                        u.getId(),
                                        u.getName(),
                                        u.getPasswordHash(),
                                        u.isEnabled(),
                                        u.getCreatedAt(),
                                        u.getUpdatedAt(),
                                        authorities,
                                        roles
                                );
                            });
                });
    }

}

