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
        BackupDTO dto = new BackupDTO();
        dto.setId(backup.getId());
        dto.setClientId(backup.getClientId());
        dto.setTaskId(backup.getTaskId());
        dto.setName(backup.getMessage());
        dto.setState(backup.getState());
        dto.setSizeBytes(backup.getSizeBytes());
        dto.setStartTime(toLdt(backup.getStartTime()));
        dto.setStopTime(toLdt(backup.getStopTime()));
        dto.setCreatedAt(toLdt(backup.getCreatedAt()));
        return dto;
    }

    public static LocalDateTime toLdt(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
