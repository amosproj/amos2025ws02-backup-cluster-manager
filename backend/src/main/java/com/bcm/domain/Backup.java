package com.bcm.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "backups")
public class Backup {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // ON DELETE SET NULL in der DB -> optionales Mapping
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "stop_time")
    private Instant stopTime;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackupState state;

    @Lob
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Backup() {}

    // Getters / Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getStopTime() { return stopTime; }
    public void setStopTime(Instant stopTime) { this.stopTime = stopTime; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public BackupState getState() { return state; }
    public void setState(BackupState state) { this.state = state; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getCreatedAt() { return createdAt; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Backup that)) return false;
        return Objects.equals(id, that.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
