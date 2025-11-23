package com.bcm.cluster_manager.controller;

import com.bcm.shared.model.database.User;
import com.bcm.shared.service.UserMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserMService userMService;

    @GetMapping
    public List<User> getAllUsers() {
        return userMService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userMService.getUserById(id);
    }

    @GetMapping("/{name}")
    public User getUser(@PathVariable String name) {
        return userMService.getUserByName(name);
    }
    @GetMapping("/search/{name}")
    public List<User> getUserByNameSearch(@PathVariable String name) {
        return userMService.getUserByNameSearch(name);
    }
    @PostMapping("/{group_id}")
    public User createUser(@PathVariable Long group_id, @RequestBody User user) {
        return userMService.addUserAndAssignGroup(user, group_id);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userMService.editUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userMService.deleteUser(id);
    }
}
