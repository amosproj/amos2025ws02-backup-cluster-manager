package com.bcm.shared.model.api;

import com.bcm.shared.model.database.TaskFrequency;
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
