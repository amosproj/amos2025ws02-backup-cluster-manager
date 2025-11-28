package com.bcm.backup_node.model.database;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class BackupData {

    private Long id;
    private String backup_data;
    private Instant createdAt;
}
