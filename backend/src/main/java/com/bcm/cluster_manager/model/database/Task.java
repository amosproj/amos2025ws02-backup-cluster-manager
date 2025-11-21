package com.bcm.cluster_manager.model.database;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Task {
    private Long id;
    private String name;
    private Long clientId;
    private String source;     // TEXT
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private String interval;
}
