package com.bcm.shared.controller;

import com.bcm.shared.model.api.UserDTO;
import com.bcm.shared.service.UserService;
import com.bcm.shared.model.database.User;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for backup node user operations: list, get, create, update, delete users.
 */
@RestController()
@RequestMapping("/api/v1/bn/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Returns a paginated list of users.
     *
     * @param pagination pagination and filter parameters
     * @return paginated response of user DTOs
     */
    @GetMapping("/userlist")
    public Mono<PaginationResponse<UserDTO>> getUsers(PaginationRequest pagination){
        return userService.getPaginatedItems(pagination);
    }

    /**
     * Generates example users for testing.
     *
     * @return success message
     */
    @GetMapping("/generateUser")
    public Mono<String> generateUser(){
        return Mono.fromRunnable(() -> userService.generateExampleUsers(50))
                .thenReturn("Generated 50 example users");
    }

    /**
     * Returns all users (no pagination).
     *
     * @return flux of all users
     */
    @GetMapping
    public Flux<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Returns a user by id.
     *
     * @param id user id
     * @return the user, or empty if not found
     */
    @GetMapping("/{id:\\d+}")
    public Mono<User> getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // Commented out to avoid ambiguity with getUserById
    // @GetMapping("/{name}")
    // public User getUser(@PathVariable String name) {
    //     return userMService.getUserByName(name);
    // }

    /**
     * Returns users whose name contains the given subtext.
     *
     * @param name name subtext to search
     * @return flux of matching users
     */
    @GetMapping("search/{name}")
    public Flux<User> getUserBySubtext(@PathVariable String name) {
        return userService.getUserBySubtext(name);
    }

    /**
     * Creates a new user and assigns them to the given group.
     *
     * @param group_id group id to assign
     * @param user     user data
     * @return the created user
     */
    @PostMapping("/{group_id}")
    public Mono<User> createUser(@PathVariable Long group_id, @RequestBody User user) {
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return userService.addUserAndAssignGroup(user, group_id);
    }

    /**
     * Updates an existing user by id.
     *
     * @param id   user id
     * @param user updated user data (password not updated if null)
     * @return the updated user
     */
    @PutMapping("/{id:\\d+}")
    public Mono<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setUpdatedAt(Instant.now());
        user.setPasswordHash(null);
        return userService.editUser(user);
    }

    /**
     * Deletes a user by id.
     *
     * @param id user id
     * @return completion when delete is done
     */
    @DeleteMapping("/{id:\\d+}")
    public Mono<Void> deleteUser(@PathVariable Long id) {
        return Mono.fromRunnable(() -> userService.deleteUser(id));
    }
}
