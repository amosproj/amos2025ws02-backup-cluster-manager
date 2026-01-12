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
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@RestController()
@RequestMapping("/api/v1/cm/users")
public class CMUserController {


    private final UserService userService;

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

    @PreAuthorize(Permission.Require.USER_READ)
    @GetMapping("/userlist")
    public Mono<PaginationResponse<UserDTO>> getUsers(PaginationRequest pagination){
        return userService.getPaginatedItems(pagination);
    }

    @GetMapping("/generateUser")
    public Mono<String> generateUser(){
        return Mono.fromRunnable(() -> userService.generateExampleUsers(50))
                .thenReturn("Generated 50 example users");
    }

    @PreAuthorize(Permission.Require.USER_READ)
    @GetMapping
    public Mono<List<User>> getAllUsers() {
        return Mono.fromCallable(() -> userService.getAllUsers());
    }

    @GetMapping("/{id:\\d+}")
    public Mono<User> getUser(@PathVariable Long id) {
        return Mono.fromCallable(() -> userService.getUserById(id));
    }

    // Commented out to avoid ambiguity with getUserById
    // @GetMapping("/{name}")
    // public User getUser(@PathVariable String name) {
    //     return userMService.getUserByName(name);
    // }

    @PreAuthorize(Permission.Require.USER_READ)
    @GetMapping("search/{name}")
    public Mono<List<User>> getUserBySubtext(@PathVariable String name) {
        return getCurrentUserRank()
                .map(requesterRank -> userService.getUserBySubtextWithRankCheck(name, requesterRank));
    }

    @PreAuthorize(Permission.Require.USER_CREATE)
    @PostMapping("/{group_id}")
    public Mono<User> createUser(@PathVariable Long group_id, @RequestBody User user) {
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return getCurrentUserRank()
                .map(requesterRank -> userService.addUserAndAssignGroupWithRankCheck(user, group_id, requesterRank));
    }

    @PreAuthorize(Permission.Require.USER_UPDATE)
    @PutMapping("/{id:\\d+}")
    public Mono<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        user.setUpdatedAt(Instant.now());
        user.setPasswordHash(null);
        return getCurrentUserRank()
                .map(requesterRank -> userService.editUserWithRankCheck(user, requesterRank));
    }

    @PreAuthorize(Permission.Require.USER_DELETE)
    @DeleteMapping("/{id:\\d+}")
    public Mono<Void> deleteUser(@PathVariable Long id) {
        return getCurrentUserRank()
                .doOnNext(requesterRank -> userService.deleteUserWithRankCheck(id, requesterRank))
                .then();
    }
}
