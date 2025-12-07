package com.bcm.shared.mapper;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.database.Backup;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class BackupConverter {

    private BackupConverter() {}

    public static BackupDTO toDTO(Backup backup) {
        if (backup == null) return null;
        return new BackupDTO(
                backup.getId(),
                backup.getClientId(),
                backup.getTaskId(),
                backup.getMessage(),
                backup.getState(),
                backup.getSizeBytes(),
                toLdt(backup.getStartTime()),
                toLdt(backup.getStopTime()),
                toLdt(backup.getCreatedAt())

        );
    }

    public static LocalDateTime toLdt(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
