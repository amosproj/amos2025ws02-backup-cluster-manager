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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
     * @return the highest rank value
     */
    private int getCurrentUserRank() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getRoles().stream()
                    .mapToInt(Role::getRank)
                    .max()
                    .orElse(0);
        }
        return 0;
    }

    @PreAuthorize(Permission.Require.USER_READ)
    @GetMapping("/userlist")
    public PaginationResponse<UserDTO> getUsers(PaginationRequest pagination){
        return userService.getPaginatedItems(pagination);
    }

    @GetMapping("/generateUser")
    public String generateUser(){
        userService.generateExampleUsers(50);
        return "Generated 50 example users";
    }

    @PreAuthorize(Permission.Require.USER_READ)
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id:\\d+}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // Commented out to avoid ambiguity with getUserById
    // @GetMapping("/{name}")
    // public User getUser(@PathVariable String name) {
    //     return userMService.getUserByName(name);
    // }

    @PreAuthorize(Permission.Require.USER_READ)
    @GetMapping("search/{name}")
    public List<User> getUserBySubtext(@PathVariable String name) {
        int requesterRank = getCurrentUserRank();
        return userService.getUserBySubtextWithRankCheck(name, requesterRank);
    }

    @PreAuthorize(Permission.Require.USER_CREATE)
    @PostMapping("/{group_id}")
    public User createUser(@PathVariable Long group_id, @RequestBody User user) {
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        int requesterRank = getCurrentUserRank();
        return userService.addUserAndAssignGroupWithRankCheck(user, group_id, requesterRank);
    }

    @PreAuthorize(Permission.Require.USER_UPDATE)
    @PutMapping("/{id:\\d+}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        user.setUpdatedAt(Instant.now());
        user.setPasswordHash(null);
        int requesterRank = getCurrentUserRank();
        return userService.editUserWithRankCheck(user, requesterRank);
    }

    @PreAuthorize(Permission.Require.USER_DELETE)
    @DeleteMapping("/{id:\\d+}")
    public void deleteUser(@PathVariable Long id) {
        int requesterRank = getCurrentUserRank();
        userService.deleteUserWithRankCheck(id, requesterRank);
    }
}
