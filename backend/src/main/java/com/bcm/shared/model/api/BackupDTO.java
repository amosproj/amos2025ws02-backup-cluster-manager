package com.bcm.shared.model.api;

import com.bcm.shared.model.database.BackupState;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<String> replicationNodes;


    public BackupDTO(Long id, Long clientId, Long taskId, String name, BackupState state,
                     Long sizeBytes, LocalDateTime startTime, LocalDateTime stopTime, LocalDateTime createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.taskId = taskId;
        this.name = name;
        this.state = state;
        this.sizeBytes = sizeBytes;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.createdAt = createdAt;
    }
}
