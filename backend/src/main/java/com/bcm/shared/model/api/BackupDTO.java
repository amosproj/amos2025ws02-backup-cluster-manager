package com.bcm.shared.model.api;

import com.bcm.shared.model.database.BackupState;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BackupDTO {
    private Long id;
    private Long clientId;
    private Long taskId;
    private String name;
    private BackupState state;
    private Long sizeBytes;
    private LocalDateTime startTime;
    private LocalDateTime stopTime;
    private LocalDateTime createdAt;

}
