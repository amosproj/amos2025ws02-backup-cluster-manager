package com.bcm.shared.controller;


import com.bcm.shared.model.api.SyncDTO;
import com.bcm.shared.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class NodeController {

    @Autowired
    private UserService userService;

    @GetMapping("/example")
    public String test(){
        return "Here is a string";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @PostMapping("/sync")
    public void sync(@RequestBody SyncDTO dto) {
        userService.replaceUsersWithCMUsers(dto.getCmUsers());
    }
}