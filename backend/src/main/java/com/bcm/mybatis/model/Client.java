package com.bcm.mybatis.model;

import java.time.Instant;
import java.util.UUID;

public class Client {
    private UUID id;
    private String nameOrIp;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNameOrIp() { return nameOrIp; }
    public void setNameOrIp(String nameOrIp) { this.nameOrIp = nameOrIp; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
