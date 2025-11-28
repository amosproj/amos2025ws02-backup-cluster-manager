package com.bcm.cluster_manager.model.api;

public class CreateBackupRequest {

    private Long clientId;
    private Long taskId;
    private Long sizeBytes;

    public CreateBackupRequest() {
    }

    public CreateBackupRequest(Long clientId, Long taskId, Long sizeBytes) {
        this.clientId = clientId;
        this.taskId = taskId;
        this.sizeBytes = sizeBytes;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }
}
