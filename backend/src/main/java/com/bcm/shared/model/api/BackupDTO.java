package com.bcm.shared.model.api;

import com.bcm.shared.model.database.BackupState;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class BackupDTO {
    private Long id;
    private Long clientId;
    private Long taskId;
    private String name;
    private BackupState state;
    private Long sizeBytes;
    private Instant  startTime;
    private Instant  stopTime;
    private Instant createdAt;

}
