package com.bcm.shared.model.database;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table("backups")
public class Backup {

    @Id
    private Long id;

    @Column("client_id")
    private Long clientId;

    @Column("task_id")
    private Long taskId;

    @Column("start_time")
    private Instant startTime;

    @Column("stop_time")
    private Instant stopTime;

    @Column("size_bytes")
    private long sizeBytes;

    @Column("state")
    private BackupState state;

    @Column("message")
    private String message;

    @Column("created_at")
    private Instant createdAt;


}
