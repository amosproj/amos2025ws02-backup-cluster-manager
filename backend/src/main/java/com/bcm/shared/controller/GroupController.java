package com.bcm.shared.controller;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/bn")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @GetMapping("/groups")
    public Mono<List<Group>> getGroups() {
        return groupService.getAllGroups();
    }

}
