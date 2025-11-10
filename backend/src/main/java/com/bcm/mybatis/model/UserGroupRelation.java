package com.bcm.mybatis.model;

import java.time.Instant;
import java.util.UUID;

public class UserGroupRelation {
    private UUID userId;
    private UUID groupId;
    private Instant addedAt;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }
    public Instant getAddedAt() { return addedAt; }
    public void setAddedAt(Instant addedAt) { this.addedAt = addedAt; }
}
