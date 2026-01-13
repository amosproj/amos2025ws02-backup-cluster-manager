package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.config.security.CustomUserDetails;
import com.bcm.cluster_manager.model.api.BigClientDTO;
import com.bcm.cluster_manager.model.api.RolePermissionDTO;
import com.bcm.cluster_manager.service.PermissionService;
import com.bcm.shared.config.permissions.Permission;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController()
@RequestMapping("/api/v1/cm")
public class CMRolePermissionController {


    @Autowired
    private PermissionService permissionService;

    @PreAuthorize(Permission.Require.PERMISSION_READ)
    @GetMapping("/permissions")
    public PaginationResponse<RolePermissionDTO> getRolePermissions(PaginationRequest pagination) {
        return permissionService.getPaginatedItems(pagination);
    }
}
