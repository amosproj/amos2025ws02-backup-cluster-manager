package com.bcm.cluster_manager.model.api;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePermissionDTO {
    private String role;
    private String permissions;
}
