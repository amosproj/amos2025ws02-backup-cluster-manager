package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.SyncService;
import com.bcm.shared.model.database.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController()
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private SyncService.UserMService userMService;

    @GetMapping
    public List<User> getAllUsers() {
        return userMService.getAllUsers();
    }

    @GetMapping("/{id:\\d+}")
    public User getUser(@PathVariable Long id) {
        return userMService.getUserById(id);
    }

    // Commented out to avoid ambiguity with getUserById
    // @GetMapping("/{name}")
    // public User getUser(@PathVariable String name) {
    //     return userMService.getUserByName(name);
    // }

    @GetMapping("search/{name}")
    public List<User> getUserBySubtext(@PathVariable String name) {
        return userMService.getUserBySubtext(name);
    }

    @PostMapping("/{group_id}")
    public User createUser(@PathVariable Long group_id, @RequestBody User user) {
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return userMService.addUserAndAssignGroup(user, group_id);
    }

    @PutMapping("/{id:\\d+}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setUpdatedAt(Instant.now());
        user.setPasswordHash(null);
        return userMService.editUser(user);
    }

    @DeleteMapping("/{id:\\d+}")
    public void deleteUser(@PathVariable Long id) {
        userMService.deleteUser(id);
    }
}
