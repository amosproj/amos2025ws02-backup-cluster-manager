package com.bcm.shared.service;

import com.bcm.shared.model.api.UserDTO;
import com.bcm.shared.model.database.User;
import com.bcm.shared.model.database.UserGroupRelation;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.UserMapper;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.PaginationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Service for managing user operations.
 * This class provides methods to handle CRUD operations for user data.
 */
@Service
public class UserService implements PaginationProvider<UserDTO> {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserGroupRelationMapper userGroupRelationMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

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