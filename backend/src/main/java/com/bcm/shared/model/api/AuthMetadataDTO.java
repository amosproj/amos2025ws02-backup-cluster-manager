package com.bcm.shared.model.api;

import com.bcm.shared.config.permissions.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthMetadataDTO {
    private String username;
    private Role role;
    private int rank;
    Set<String> permissions;
}
