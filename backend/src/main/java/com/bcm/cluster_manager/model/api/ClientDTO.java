package com.bcm.cluster_manager.model.api;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ClientDTO {
    private Long id;
    private String nameOrIp;
    private boolean enabled;
}
