package com.bcm.shared.model.database;

import java.time.Instant;
import java.util.UUID;

public class Backup {
    private UUID id;
    private UUID clientId;
    private UUID taskId;       // nullable
    private Instant startTime;
    private Instant stopTime;  // nullable
    private long sizeBytes;
    private String state;      // e.g. COMPLETED, FAILED, ...
    private String message;    // nullable (TEXT)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getClientId() { return clientId; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }
    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getStopTime() { return stopTime; }
    public void setStopTime(Instant stopTime) { this.stopTime = stopTime; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
