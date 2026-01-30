package com.bcm.shared.controller;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller for backup node groups: list all groups.
 */
@RestController()
@RequestMapping("/api/v1/bn")
public class GroupController {

    @Autowired
    private GroupService groupService;

    /**
     * Returns all groups.
     *
     * @return list of groups
     */
    @GetMapping("/groups")
    public Mono<List<Group>> getGroups() {
        return groupService.getAllGroups();
    }

}
