package com.bcm.cluster_manager.controller;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.service.GroupService;
import com.bcm.shared.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/cm")
public class CMGroupController {

    private final GroupService groupService;

    public CMGroupController(@Qualifier("groupServiceCM") GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/groups")
    public List<Group> getGroups() {
        return groupService.getAllGroups();
    }

}
