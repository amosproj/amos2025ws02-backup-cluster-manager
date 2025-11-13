package com.bcm.shared.model.database;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
public class Backup {

    private Long id;
    private Long clientId;
    private Long taskId;       // nullable
    private Instant startTime;
    private Instant stopTime;  // nullable
    private long sizeBytes;
    private String state;      // e.g. COMPLETED, FAILED, ...
    private String message;    // nullable (TEXT)
    private Instant createdAt;


}
