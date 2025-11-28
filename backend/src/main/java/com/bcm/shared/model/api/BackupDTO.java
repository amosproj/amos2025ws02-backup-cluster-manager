package com.bcm.shared.model.api;

import com.bcm.cluster_manager.model.database.BackupState;

import java.time.LocalDateTime;
import java.util.List;

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
                     Long sizeBytes, LocalDateTime startTime, LocalDateTime stopTime, LocalDateTime createdAt, List<String> replicationNodes) {
        this.id = id;
        this.clientId = clientId;
        this.taskId = taskId;
        this.name = name;
        this.state = state;
        this.sizeBytes = sizeBytes;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.createdAt = createdAt;
        this.replicationNodes = replicationNodes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BackupState getState() { return state; }
    public void setStatus(BackupState state) { this.state = state; }

    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getStopTime() { return stopTime; }
    public void setStopTime(LocalDateTime stopTime) { this.stopTime = stopTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getReplicationNodes() { return replicationNodes; }
    public void setReplicationNodes(List<String> replicationNodes) { this.replicationNodes = replicationNodes; }
}
