package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.config.security.CustomUserDetails;
import com.bcm.shared.config.permissions.Role;
import com.bcm.shared.model.database.Group;
import com.bcm.shared.service.GroupService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @GetMapping("/groups")
    public List<Group> getGroups() {
        int requesterRank = getCurrentUserRank();
        return groupService.getAllGroupsWithRankCheck(requesterRank);
    }

}
