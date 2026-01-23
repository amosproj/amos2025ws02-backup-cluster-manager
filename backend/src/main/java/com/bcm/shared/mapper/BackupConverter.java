package com.bcm.shared.mapper;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.database.Backup;

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
        dto.setStartTime(backup.getStartTime());
        dto.setStopTime(backup.getStopTime());
        dto.setCreatedAt(backup.getCreatedAt());
        return dto;
    }
}
