package com.bcm.shared.model.database;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table
public class Task {
    @Id
    private Long id;

    private String name;

    @Column("client_id")
    private Long clientId;

    private String source;

    private boolean enabled;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    @Column("interval")
    private TaskFrequency interval;
}
