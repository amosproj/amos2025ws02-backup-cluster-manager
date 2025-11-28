package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.model.api.UserDTO;
import com.bcm.cluster_manager.service.UserService;
import com.bcm.cluster_manager.model.database.User;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController()
@RequestMapping("/api/v1/cm/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/userlist")
    public PaginationResponse<UserDTO> getUsers(PaginationRequest pagination){
        return userService.getPaginatedItems(pagination);
    }

    @GetMapping("/generateUser")
    public String generateUser(){
        userService.generateExampleUsers(50);
        return "Generated 50 example users";
    }

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

    @GetMapping("search/{name}")
    public List<User> getUserBySubtext(@PathVariable String name) {
        return userService.getUserBySubtext(name);
    }

    @PostMapping("/{group_id}")
    public User createUser(@PathVariable Long group_id, @RequestBody User user) {
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return userService.addUserAndAssignGroup(user, group_id);
    }

    @PutMapping("/{id:\\d+}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setUpdatedAt(Instant.now());
        user.setPasswordHash(null);
        return userService.editUser(user);
    }

    @DeleteMapping("/{id:\\d+}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
