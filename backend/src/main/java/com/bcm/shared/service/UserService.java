package com.bcm.shared.service;

import com.bcm.shared.config.permissions.Role;
import com.bcm.shared.model.api.UserDTO;
import com.bcm.shared.model.database.User;
import com.bcm.shared.model.database.UserGroupRelation;
import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.UserMapper;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.PaginationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing user operations.
 * This class provides methods to handle CRUD operations for user data.
 */
@Service
public class UserService implements PaginationProvider<UserDTO> {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    private final UserGroupRelationMapper userGroupRelationMapper;

    private final GroupMapper groupMapper;

    public UserService(@Qualifier("userGroupRelationMapperBN") UserGroupRelationMapper userGroupRelationMapper,
                      @Qualifier("userMapperBN") UserMapper userMapper,
                      @Qualifier("groupMapperBN") GroupMapper groupMapper) {
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
    @Transactional
    public User getUserById(Long id) {
        return userMapper.findById(id);
    }

    /**
     * Retrieves a user by their name.
     *
     * @param name the name of the user to retrieve
     * @return the user with the specified name, or null if no user is found
     */
    @Transactional
    public User getUserByName(String name) {
        return userMapper.findByName(name);
    }

    /**
     * Retrieves a user by their name.
     *
     * @param name the name of the user to retrieve
     * @return the user with the specified name, or null if no user is found
     */
    @Transactional
    public List<User> getUserBySubtext(String name) {
        return userMapper.findByNameSubtext(name);
    }

    /**
     * Retrieves a list of all users.
     *
     * @return a list of User objects representing all users stored in the repository
     */
    @Transactional
    public List<User> getAllUsers() {
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
    public User addUserAndAssignGroup(User user, Long groupID) {
        // Groups give users permissions. So a user without a group would be awkward.
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        userMapper.insert(user);
        UserGroupRelation userGroupRelation = new UserGroupRelation();
        userGroupRelation.setUserId(user.getId());
        userGroupRelation.setGroupId(groupID);
        userGroupRelation.setAddedAt(Instant.now());
        userGroupRelationMapper.insert(userGroupRelation);
        return userMapper.findById(user.getId());
    }

    /**
     * Updates the information of an existing user and retrieves the updated user record.
     *
     * @param user the user object containing updated data. The user must have an existing ID for the update to be performed.
     * @return the user object with updated information, retrieved from the database after the update.
     */
    @Transactional
    public User editUser(User user) {
        userMapper.update(user);
        return userMapper.findById(user.getId());
    }

    /**
     * Deletes a user by their unique identifier.
     *
     * @param id the unique identifier of the user to delete
     * @return true if a user was successfully deleted, false otherwise
     */
    @Transactional
    public boolean deleteUser(Long id) {
        // deleting a user should also delete all related user-group relations through cascading
        return userMapper.delete(id) == 1;
    }

    /**
     * Gets the highest rank of a user based on their groups.
     *
     * @param userId the ID of the user
     * @return the highest rank value, or 0 if the user has no groups/roles
     */
    @Transactional
    public int getUserHighestRank(Long userId) {
        var relations = userGroupRelationMapper.findByUser(userId);
        return relations.stream()
                .map(rel -> groupMapper.findById(rel.getGroupId()))
                .filter(group -> group != null && group.isEnabled())
                .map(group -> {
                    try {
                        return Role.valueOf(group.getName().toUpperCase().replace(' ', '_'));
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(role -> role != null)
                .mapToInt(Role::getRank)
                .max()
                .orElse(0);
    }

    /**
     * Filters users to only include those with a lower rank than the requester.
     *
     * @param users the list of users to filter
     * @param requesterRank the rank of the requesting user
     * @return filtered list of users with lower rank
     */
    private List<User> filterUsersByRank(List<User> users, int requesterRank) {
        return users.stream()
                .filter(user -> getUserHighestRank(user.getId()) < requesterRank)
                .collect(Collectors.toList());
    }

    /**
     * Checks if the requester has permission to manage a target user based on rank.
     *
     * @param targetUserId the ID of the user to be managed
     * @param requesterRank the rank of the requesting user
     * @throws AccessDeniedException if the requester's rank is not higher than the target user's rank
     */
    private void validateRankPermission(Long targetUserId, int requesterRank) {
        int targetRank = getUserHighestRank(targetUserId);
        if (targetRank >= requesterRank) {
            throw new AccessDeniedException("Insufficient rank to manage this user");
        }
    }

    /**
     * Retrieves users by name subtext, filtered by requester's rank.
     * Only returns users with a lower rank than the requester.
     *
     * @param name the name subtext to search for
     * @param requesterRank the rank of the requesting user
     * @return list of users with lower rank matching the name subtext
     */
    @Transactional
    public List<User> getUserBySubtextWithRankCheck(String name, int requesterRank) {
        List<User> users = userMapper.findByNameSubtext(name);
        return filterUsersByRank(users, requesterRank);
    }

    /**
     * Updates a user with rank validation.
     * Only allows updating users with a lower rank than the requester.
     *
     * @param user the user object containing updated data
     * @param requesterRank the rank of the requesting user
     * @return the updated user object
     * @throws AccessDeniedException if the requester's rank is not higher than the target user's rank
     */
    @Transactional
    public User editUserWithRankCheck(User user, int requesterRank) {
        validateRankPermission(user.getId(), requesterRank);
        userMapper.update(user);
        return userMapper.findById(user.getId());
    }

    /**
     * Deletes a user with rank validation.
     * Only allows deleting users with a lower rank than the requester.
     *
     * @param id the unique identifier of the user to delete
     * @param requesterRank the rank of the requesting user
     * @return true if a user was successfully deleted, false otherwise
     * @throws AccessDeniedException if the requester's rank is not higher than the target user's rank
     */
    @Transactional
    public boolean deleteUserWithRankCheck(Long id, int requesterRank) {
        validateRankPermission(id, requesterRank);
        return userMapper.delete(id) == 1;
    }

    /**
     * Adds a new user with rank validation on the target group.
     * Only allows creating users in groups with a lower rank than the requester.
     *
     * @param user the user object to be added
     * @param groupID the ID of the group to assign to the user
     * @param requesterRank the rank of the requesting user
     * @return the newly created user object
     * @throws AccessDeniedException if the requester's rank is not higher than the target group's rank
     */
    @Transactional
    public User addUserAndAssignGroupWithRankCheck(User user, Long groupID, int requesterRank) {
        // Check if the requester has higher rank than the target group
        var targetGroup = groupMapper.findById(groupID);
        if (targetGroup != null && targetGroup.isEnabled()) {
            try {
                Role targetRole = Role.valueOf(targetGroup.getName().toUpperCase().replace(' ', '_'));
                if (targetRole.getRank() >= requesterRank) {
                    throw new AccessDeniedException("Insufficient rank to create user in this group");
                }
            } catch (IllegalArgumentException e) {
                // Group doesn't map to a valid role, allow it
            }
        }

        // Use the existing method to add user and assign group
        return addUserAndAssignGroup(user, groupID);
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
            userMapper.insert(user);
        }
    }

    @Override
    public long getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        Boolean isUserEnabled = getUserFilter(filter);
        return userMapper.getTotalCount(filter.getSearch(), isUserEnabled);

    }

    @Override
    public List<UserDTO> getDBItems(long page, long itemsPerPage, Filter filter) {
        // Add SQL query with filter and pagination to get the actual items
        Boolean isUserEnabled = getUserFilter(filter);

        List<User> users = userMapper.getPaginatedAndFilteredUsers(page, itemsPerPage, filter.getSearch(), filter.getSortBy(), filter.getSortOrder(), isUserEnabled);
        return users.stream().map(UserDTO::fromUser).toList();
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