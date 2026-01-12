package com.bcm.shared.controller;

import com.bcm.shared.model.api.UserDTO;
import com.bcm.shared.service.UserService;
import com.bcm.shared.model.database.User;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@RestController()
@RequestMapping("/api/v1/bn/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/userlist")
    public Mono<PaginationResponse<UserDTO>> getUsers(PaginationRequest pagination){
        return userService.getPaginatedItems(pagination);
    }

    @GetMapping("/generateUser")
    public Mono<String> generateUser(){
        return Mono.fromRunnable(() -> userService.generateExampleUsers(50))
                .thenReturn("Generated 50 example users");
    }

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

    @GetMapping("search/{name}")
    public Mono<List<User>> getUserBySubtext(@PathVariable String name) {
        return Mono.fromCallable(() -> userService.getUserBySubtext(name));
    }

    @PostMapping("/{group_id}")
    public Mono<User> createUser(@PathVariable Long group_id, @RequestBody User user) {
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return Mono.fromCallable(() -> userService.addUserAndAssignGroup(user, group_id));
    }

    @PutMapping("/{id:\\d+}")
    public Mono<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setUpdatedAt(Instant.now());
        user.setPasswordHash(null);
        return Mono.fromCallable(() -> userService.editUser(user));
    }

    @DeleteMapping("/{id:\\d+}")
    public Mono<Void> deleteUser(@PathVariable Long id) {
        return Mono.fromRunnable(() -> userService.deleteUser(id));
    }
}
