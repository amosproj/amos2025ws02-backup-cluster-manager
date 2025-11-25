package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.repository.ClientRepository;
import com.bcm.cluster_manager.repository.TaskRepository;
import com.bcm.shared.model.api.ClusterTablesDTO;
import com.bcm.shared.model.database.User;
import com.bcm.shared.model.database.UserGroupRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SyncService {
    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    @Autowired
    private RegistryService registry;

    private final RestTemplate rest = new RestTemplate();

    public void pushTablesToAllNodes() {
        ClusterTablesDTO tables = new ClusterTablesDTO(registry.getActiveNodes(), registry.getInactiveNodes());
        registry.getActiveNodes().forEach(node -> asyncPush(node.getAddress(), tables));
    }

    @Async
    protected CompletableFuture<Void> asyncPush(String address, ClusterTablesDTO dto) {
        String url = "http://" + address + "/api/v1/sync";
        try {
            rest.postForEntity(url, dto, Void.class);
            logger.info("Pushed tables to {}", address);
        } catch (Exception e) {
            logger.warn("Failed to push tables to {}: {}", address, e.toString());
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Service for managing user operations.
     * This class provides methods to handle CRUD operations for user data.
     */
    @Service
    public static class UserMService {

        final TaskRepository.UserMapper userMapper;
        final ClientRepository.UserGroupRelationMapper userGroupRelationMapper;

        public UserMService(TaskRepository.UserMapper userMapper, ClientRepository.UserGroupRelationMapper userGroupRelationMapper) {
            this.userMapper = userMapper;
            this.userGroupRelationMapper = userGroupRelationMapper;
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
        public User getUserByName(String name){
            return userMapper.findByName(name);
        }

        /**
         * Retrieves a user by their name.
         *
         * @param name the name of the user to retrieve
         * @return the user with the specified name, or null if no user is found
         */
        @Transactional
        public List<User> getUserBySubtext(String name){
            return userMapper.findByNameSubtext(name);
        }

        /**
         * Retrieves a list of all users.
         *
         * @return a list of User objects representing all users stored in the repository
         */
        @Transactional
        public List<User> getAllUsers(){
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
        public User addUserAndAssignGroup(User user, Long groupID){
            // Groups give users permissions. So a user without a group would be awkward.
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
        public User editUser(User user){
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
        public boolean deleteUser(Long id){
            // deleting a user should also delete all related user-group relations through cascading
            return userMapper.delete(id) == 1;
        }
    }
}