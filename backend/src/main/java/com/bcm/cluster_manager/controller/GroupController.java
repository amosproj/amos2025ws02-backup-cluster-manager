package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.model.database.Group;
import com.bcm.cluster_manager.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/v1")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @GetMapping("/groups")
    public List<Group> getGroups() {
        return groupService.getAllGroups();
    }

}
