package com.bcm.shared.service;

import com.bcm.shared.model.api.*;
import com.bcm.shared.model.database.Backup;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.repository.BackupMapper;
import com.bcm.shared.repository.BackupMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class BackupService {

    private final BackupMapper backupMapper;

    public BackupService(BackupMapper backupMapper) {
        this.backupMapper = backupMapper;
    }

    public BackupDTO createBackup(CreateBackupRequest dto) {
        return store(dto);
    }

    public void deleteBackup(Long backupId) {
            backupMapper.delete(backupId);
    }

    public void executeBackupSync(Long id, ExecuteBackupRequest request) {
        // Mock implementation: In a real scenario, this would trigger the backup process.
        // wait request.getDuration() and then return
        long now = System.currentTimeMillis();

         try {
            Thread.sleep(request.getDuration());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }


    public BackupDTO store(CreateBackupRequest dto) {
        Backup backup = new Backup();
        backup.setClientId(dto.getClientId());
        backup.setTaskId(dto.getTaskId());
        backup.setSizeBytes(dto.getSizeBytes() != null ? dto.getSizeBytes() : 0L);
        backup.setStartTime(Instant.now());
        backup.setState(BackupState.QUEUED);
        backup.setMessage(null);
        backup.setCreatedAt(Instant.now());

        backupMapper.insert(backup);
        return new BackupDTO(
                backup.getId(),
                backup.getClientId(),
                backup.getTaskId(),
                "Backup-" + backup.getTaskId(),
                backup.getState(),
                backup.getSizeBytes(),
                toLdt(backup.getStartTime()),
                toLdt(backup.getStopTime()),
                toLdt(backup.getCreatedAt())
        );

    }

    public static LocalDateTime toLdt(Instant t) {
        return t == null ? null : LocalDateTime.ofInstant(t, ZoneOffset.UTC);
    }

    private BackupDTO toDTO(Backup metadata) {
        return new BackupDTO(
                metadata.getId(),
                metadata.getClientId(),
                metadata.getTaskId(),
                "Backup-" + metadata.getId(),
                metadata.getState(),
                metadata.getSizeBytes(),
                toLdt(metadata.getStartTime()),
                toLdt(metadata.getStopTime()),
                toLdt(metadata.getCreatedAt())
        );
    }
}
