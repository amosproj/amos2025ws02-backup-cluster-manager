package com.bcm.cluster_manager.model.api;

import com.bcm.cluster_manager.model.database.TaskFrequency;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private String name;
    private Long clientId;
    private String source;     // TEXT
    private boolean enabled;
    private TaskFrequency interval;
}
