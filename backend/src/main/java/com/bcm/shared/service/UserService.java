package com.bcm.shared.service;

import com.bcm.shared.config.permissions.Role;
import com.bcm.shared.model.api.UserDTO;
import com.bcm.shared.model.database.Group;
import com.bcm.shared.model.database.User;
import com.bcm.shared.model.database.UserGroupRelation;
import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.UserMapper;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.PaginationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing user operations.
 * This class provides methods to handle CRUD operations for user data.
 */
@Service
public class UserService implements PaginationProvider<UserDTO> {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final Object userReplaceLock = new Object();

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    private final UserGroupRelationMapper userGroupRelationMapper;

    private final GroupMapper groupMapper;

    public UserService( UserGroupRelationMapper userGroupRelationMapper,
                        UserMapper userMapper,
                        GroupMapper groupMapper) {
        this.userGroupRelationMapper = userGroupRelationMapper;
        this.userMapper = userMapper;
        this.groupMapper = groupMapper;
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the unique identifier of the user
     * @return the user with the specified ID, or null if no user is found
     */
    public Mono<User> getUserById(Long id) {
        return userMapper.findUserById(id);
    }

    /**
     * Retrieves a user by their name.
     *
     * @param name the name of the user to retrieve
     * @return the user with the specified name, or null if no user is found
     */
    public Mono<User> getUserByName(String name) {
        return userMapper.findByName(name);
    }

    /**
     * Retrieves a user by their name.
     *
     * @param name the name of the user to retrieve
     * @return the user with the specified name, or null if no user is found
     */
    public Flux<User> getUserBySubtext(String name) {
        return userMapper.findByNameSubtext(name);
    }

    /**
     * Retrieves a list of all users.
     *
     * @return a list of User objects representing all users stored in the repository
     */
    public Flux<User> getAllUsers() {
        return userMapper.findAll();
    }

    /**
     * Adds a new user to the system, assigns the user to the specified group and retrieves the created user from the repository.
     *
     * @param user    the user object to be added to the repository
     * @param groupID the ID of the group to assign to the user
     * @return the user object representing the newly created user, including the generated ID
     */
    @Transactional
    public Mono<User> addUserAndAssignGroup(User user, Long groupID) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setId(null);

        return userMapper.save(user)
                .flatMap(saved -> {
                    UserGroupRelation rel = new UserGroupRelation();
                    rel.setUserId(saved.getId());
                    rel.setGroupId(groupID);
                    rel.setAddedAt(Instant.now());
                    return userGroupRelationMapper.save(rel).thenReturn(saved);
                })
                .flatMap(saved -> userMapper.findUserById(saved.getId()));
    }

    /**
     * Updates the information of an existing user and retrieves the updated user record.
     *
     * @param user the user object containing updated data. The user must have an existing ID for the update to be performed.
     * @return the user object with updated information, retrieved from the database after the update.
     */
    public Mono<User> editUser(User user) {
        return userMapper.save(user).flatMap(u -> userMapper.findUserById(u.getId()));
    }

    @Transactional
    public Mono<Void> replaceUsersWithCMUsers(List<User> cmUsers) {

        // lock to prevent concurrent replacements
            // as of now, we simply delete all users and re-insert from shared CM user table
            // in the future, we might want to do a more intelligent merge, but currently no way to identify node-wise unique users to only update changes
        return Mono.defer(() -> {
            synchronized (userReplaceLock) {
                return userMapper.deleteAll()
                        .thenMany(Flux.fromIterable(cmUsers)
                                .concatMap(u -> { u.setId(null); return userMapper.save(u); }))
                        .then();
            }
        });


    }

    /*
    private void insertUsers(List<User> users) {
        users.forEach(user -> {
            userMapper.save(user);
            logger.info("Inserted user {}", user.getName());

        });
    }

    private Mono<Void> deleteAllUsers() {
        return userMapper.deleteAll();
    }
    */
    /**
     * Deletes a user by their unique identifier.
     *
     * @param id the unique identifier of the user to delete
     * @return true if a user was successfully deleted, false otherwise
     */
    public Mono<Boolean> deleteUser(Long id) {
        return userMapper.deleteUserById(id).thenReturn(true);
    }

    /**
     * Gets the highest rank of a user based on their groups.
     *
     * @param userId the ID of the user
     * @return the highest rank value, or 0 if the user has no groups/roles
     */
    public Mono<Integer>  getUserHighestRank(Long userId) {
        return userGroupRelationMapper.findByUser(userId)
                .map(UserGroupRelation::getGroupId)
                .distinct()
                .collectList()
                .flatMapMany(groupMapper::findAllById)   // avoids N+1
                .filter(Group::isEnabled)
                .mapNotNull(group -> {
                    try {
                        return Role.valueOf(group.getName().toUpperCase().replace(' ', '_'));
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(role -> role != null ? role.getRank() : 0)
                .reduce(0, Math::max);

    }

    /**
     * Filters users to only include those with a lower rank than the requester.
     *
     * @param users         the list of users to filter
     * @param requesterRank the rank of the requesting user
     * @return filtered list of users with lower rank
     */
    public Flux<User> filterUsersByRank(Flux<User> users, int requesterRank) {
        return users.flatMap(user ->
                getUserHighestRank(user.getId())
                        .filter(rank -> rank < requesterRank)
                        .map(r -> user)
        );
    }

    /**
     * Checks if the requester has permission to manage a target user based on rank.
     *
     * @param targetUserId  the ID of the user to be managed
     * @param requesterRank the rank of the requesting user
     * @throws AccessDeniedException if the requester's rank is not higher than the target user's rank
     */
    private Mono<Void> validateRankPermission(Long targetUserId, int requesterRank) {
        return getUserHighestRank(targetUserId)
                .flatMap(targetRank -> {
                    if (targetRank >= requesterRank) {
                        return Mono.error(new AccessDeniedException("Insufficient rank to manage this user"));
                    }
                    return Mono.empty();
                });
    }


    /**
     * Retrieves users by name subtext, filtered by requester's rank.
     * Only returns users with a lower rank than the requester.
     *
     * @param name          the name subtext to search for
     * @param requesterRank the rank of the requesting user
     * @return list of users with lower rank matching the name subtext
     */
    public Mono<List<User>> getUserBySubtextWithRankCheck(String name, int requesterRank) {
        return userMapper.findByNameSubtext(name)
                .filterWhen(user -> getUserHighestRank(user.getId())
                        .map(rank -> rank < requesterRank))
                .collectList();
    }

    /**
     * Updates a user with rank validation.
     * Only allows updating users with a lower rank than the requester.
     *
     * @param user          the user object containing updated data
     * @param requesterRank the rank of the requesting user
     * @return the updated user object
     * @throws AccessDeniedException if the requester's rank is not higher than the target user's rank
     */
    public Mono<User> editUserWithRankCheck(User user, int requesterRank) {
        return validateRankPermission(user.getId(), requesterRank).then(userMapper.save(user));
    }
    /**
     * Deletes a user with rank validation.
     * Only allows deleting users with a lower rank than the requester.
     *
     * @param id            the unique identifier of the user to delete
     * @param requesterRank the rank of the requesting user
     * @return true if a user was successfully deleted, false otherwise
     * @throws AccessDeniedException if the requester's rank is not higher than the target user's rank
     */
    @Transactional
    public Mono<Boolean> deleteUserWithRankCheck(Long id, int requesterRank) {
        return validateRankPermission(id, requesterRank)
                .then(userMapper.existsUserById(id))
                .flatMap(exists ->
                        exists ? userMapper.deleteUserById(id).thenReturn(true)
                                : Mono.just(false)
                );
    }

    /**
     * Adds a new user with rank validation on the target group.
     * Only allows creating users in groups with a lower rank than the requester.
     *
     * @param user          the user object to be added
     * @param groupID       the ID of the group to assign to the user
     * @param requesterRank the rank of the requesting user
     * @return the newly created user object
     * @throws AccessDeniedException if the requester's rank is not higher than the target group's rank
     */
    @Transactional
    public Mono<User> addUserAndAssignGroupWithRankCheck(User user, Long groupId, int requesterRank) {

        return groupMapper.findById(groupId)
                .flatMap(targetGroup -> {
                    if (!targetGroup.isEnabled()) {
                        return Mono.just(targetGroup);
                    }
                    try {
                        Role targetRole = Role.valueOf(targetGroup.getName().toUpperCase().replace(' ', '_'));
                        if (targetRole.getRank() >= requesterRank) {
                            return Mono.error(new AccessDeniedException("Insufficient rank to create user in this group"));
                        }
                    } catch (IllegalArgumentException ignored) {
                    }

                    return Mono.just(targetGroup);
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Group not found: " + groupId)))
                .then(addUserAndAssignGroup(user, groupId));
    }


    // Generate example user for testing purposes
    public void generateExampleUsers(long amount) {
        for (long i = 1; i <= amount; i++) {
            User user = new User();
            user.setName("example_user_" + i);
            user.setPasswordHash("hashed_password_" + i);
            user.setEnabled(true);
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            userMapper.save(user);
        }
    }

    @Override
    public Mono<Long> getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        Boolean isUserEnabled = getUserFilter(filter);
        return userMapper.getTotalCount(filter.getSearch(), isUserEnabled);

    }

    public Mono<List<UserDTO>> getDBItems(long page, long itemsPerPage, Filter filter) {
        Boolean enabled = getUserFilter(filter);

        return userMapper
                .getPaginatedAndFilteredUsers(page, itemsPerPage, filter.getSearch(),
                        filter.getSortBy(), filter.getSortOrder(), enabled)
                .map(UserDTO::fromUser)
                .collectList();
    }


    private Boolean getUserFilter(Filter filter) {
        Set<String> filters = filter.getFilters();
        if (filters != null && !filters.isEmpty()) {
            if (filters.contains("Enabled") && !filters.contains("Disabled")) {
                // Only enabled users
                return true;
            } else if (!filters.contains("Enabled") && filters.contains("Disabled")) {
                // Only disabled users
                return false;
            }
        }
        return null;
    }
}