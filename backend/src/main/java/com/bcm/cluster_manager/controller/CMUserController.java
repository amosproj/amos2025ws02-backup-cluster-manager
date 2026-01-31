package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.config.security.CustomUserDetails;
import com.bcm.shared.config.permissions.Permission;
import com.bcm.shared.config.permissions.Role;
import com.bcm.shared.model.api.UserDTO;
import com.bcm.shared.model.database.User;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import com.bcm.shared.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for cluster manager users: list, get, create, update, delete users with rank-based access.
 */
@RestController()
@RequestMapping("/api/v1/cm/users")
public class CMUserController {


    private final UserService userService;

    /**
     * Creates the CM user controller with the CM-specific user service.
     *
     * @param userService user service qualified for cluster manager
     */
    public CMUserController(@Qualifier("userServiceCM") UserService userService) {
        this.userService = userService;
    }

    /**
     * Gets the highest rank of the currently authenticated user.
     *
     * @return a Mono containing the highest rank value
     */
    private Mono<Integer> getCurrentUserRank() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> {
                    if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                        return userDetails.getRoles().stream()
                                .mapToInt(Role::getRank)
                                .max()
                                .orElse(0);
                    }
                    return 0;
                })
                .defaultIfEmpty(0);
    }

    /**
     * Returns a paginated list of users.
     *
     * @param pagination pagination and filter parameters
     * @return paginated response of user DTOs
     */
    @PreAuthorize(Permission.Require.USER_READ)
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
    @PreAuthorize(Permission.Require.USER_READ)
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

    /**
     * Returns users whose name contains the given subtext, filtered by requester rank.
     *
     * @param name name subtext to search
     * @return list of matching users with lower rank than requester
     */
    // Commented out to avoid ambiguity with getUserById
    // @GetMapping("/{name}")
    // public User getUser(@PathVariable String name) {
    //     return userMService.getUserByName(name);
    // }

    @PreAuthorize(Permission.Require.USER_READ)
    @GetMapping("search/{name}")
    public Mono<List<User>> getUserBySubtext(@PathVariable String name) {
        return getCurrentUserRank()
                .flatMap(requesterRank -> userService.getUserBySubtextWithRankCheck(name, requesterRank));
    }

    /**
     * Creates a new user and assigns them to the given group (with rank check).
     *
     * @param group_id group id to assign
     * @param user     user data
     * @return the created user
     */
    @PreAuthorize(Permission.Require.USER_CREATE)
    @PostMapping("/{group_id}")
    public Mono<User> createUser(@PathVariable Long group_id, @RequestBody User user) {
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return getCurrentUserRank()
                .flatMap(requesterRank -> userService.addUserAndAssignGroupWithRankCheck(user, group_id, requesterRank));
    }

    /**
     * Updates an existing user by id (with rank check).
     *
     * @param id   user id
     * @param user updated user data
     * @return the updated user
     */
    @PreAuthorize(Permission.Require.USER_UPDATE)
    @PutMapping("/{id:\\d+}")
    public Mono<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        user.setUpdatedAt(Instant.now());
        user.setPasswordHash(null);
        return getCurrentUserRank()
                .flatMap(requesterRank -> userService.editUserWithRankCheck(user, requesterRank));
    }

    /**
     * Deletes a user by id (with rank check).
     *
     * @param id user id
     * @return completion when delete is done
     */
    @PreAuthorize(Permission.Require.USER_DELETE)
    @DeleteMapping("/{id:\\d+}")
    public Mono<Void> deleteUser(@PathVariable Long id) {
        return getCurrentUserRank()
                .flatMap(requesterRank -> userService.deleteUserWithRankCheck(id, requesterRank))
                .then();
    }
}
