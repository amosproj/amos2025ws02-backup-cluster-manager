package com.bcm.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    private UUID id;

    @Column(name = "name_or_ip", nullable = false, unique = true, length = 255)
    private String nameOrIp;

    @Column(nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Client() {}

    // Getters / Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNameOrIp() { return nameOrIp; }
    public void setNameOrIp(String nameOrIp) { this.nameOrIp = nameOrIp; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client that)) return false;
        return Objects.equals(id, that.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
