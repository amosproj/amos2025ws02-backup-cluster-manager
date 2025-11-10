package com.bcm.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_group_relations")
@IdClass(UserGroupRelation.Key.class)
public class UserGroupRelation {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    // optionale Navigations-Refs (Read-only, da insertable/updatable=false)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private Group group;

    public UserGroupRelation() {}

    public UserGroupRelation(UUID userId, UUID groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }

    // Getters / Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }
    public Instant getAddedAt() { return addedAt; }
    public User getUser() { return user; }
    public Group getGroup() { return group; }

    // Composite-Key Klasse
    public static class Key implements Serializable {
        private UUID userId;
        private UUID groupId;

        public Key() {}
        public Key(UUID userId, UUID groupId) {
            this.userId = userId;
            this.groupId = groupId;
        }

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public UUID getGroupId() { return groupId; }
        public void setGroupId(UUID groupId) { this.groupId = groupId; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key key)) return false;
            return Objects.equals(userId, key.userId) &&
                    Objects.equals(groupId, key.groupId);
        }
        @Override public int hashCode() { return Objects.hash(userId, groupId); }
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserGroupRelation that)) return false;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(groupId, that.groupId);
    }
    @Override public int hashCode() { return Objects.hash(userId, groupId); }
}
