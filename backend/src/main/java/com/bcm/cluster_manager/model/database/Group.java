package com.bcm.cluster_manager.model.database;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Group {
    private Long id;
    private String name;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

}
