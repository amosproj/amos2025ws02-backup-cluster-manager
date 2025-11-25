package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.repository.TaskMapper;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.database.Backup;
import com.bcm.shared.model.database.BackupState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class BackupStorageService {

    private final TaskMapper.BackupMapper backupMapper;

    public BackupStorageService(TaskMapper.BackupMapper backupMapper) {
        this.backupMapper = backupMapper;
    }

    @Transactional
    public BackupDTO store(BackupDTO dto) {
        Backup backup = new Backup();
        backup.setClientId(dto.getClientId());
        backup.setTaskId(dto.getTaskId());
        backup.setSizeBytes(dto.getSizeBytes() != null ? dto.getSizeBytes() : 0L);
        backup.setStartTime(Instant.now());
        backup.setState(BackupState.RUNNING);
        backup.setMessage(null);
        backup.setCreatedAt(Instant.now());

        backupMapper.insert(backup);
        return toDTO(backup);
    }

    public static LocalDateTime toLdt(Instant t) {
        return t == null ? null : LocalDateTime.ofInstant(t, ZoneOffset.UTC);
    }

    private BackupDTO toDTO(Backup backup) {
        return new BackupDTO(
                backup.getId(),
                backup.getClientId(),
                backup.getTaskId(),
                "Backup-" + backup.getTaskId(),
                backup.getState(),
                backup.getSizeBytes(),
                backup.getStartTime() != null
                        ? LocalDateTime.ofInstant(backup.getStartTime(), ZoneOffset.UTC) : null,
                backup.getStopTime() != null
                        ? LocalDateTime.ofInstant(backup.getStopTime(), ZoneOffset.UTC) : null,
                backup.getCreatedAt() != null
                        ? LocalDateTime.ofInstant(backup.getCreatedAt(), ZoneOffset.UTC) : null,
                null
        );

    }
}
