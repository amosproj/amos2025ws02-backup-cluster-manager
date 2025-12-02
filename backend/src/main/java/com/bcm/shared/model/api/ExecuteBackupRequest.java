package com.bcm.shared.model.api;

import java.util.List;

public class ExecuteBackupRequest {
    private Long duration;        // milliseconds
    private Boolean shouldSucceed;
    private List<String> nodes;

    public ExecuteBackupRequest() {
    }

    public  ExecuteBackupRequest(Long duration, Boolean shouldSucceed, List<String> nodes) {
        this.duration = duration;
        this.shouldSucceed = shouldSucceed;
        this.nodes = nodes;
    }
    // getters and setters
    public Long getDuration() {
        return duration;
    }
    public void setDuration(Long duration) {
        this.duration = duration;
    }
    public Boolean getShouldSucceed() {
        return shouldSucceed;
    }
    public void setShouldSucceed(Boolean shouldSucceed) {
        this.shouldSucceed = shouldSucceed;
    }
    public List<String> getNodes() {
        return nodes;
    }
    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

}