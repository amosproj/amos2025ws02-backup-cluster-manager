package com.bcm.shared.service;

import com.bcm.cluster_manager.repository.TaskRepository;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.database.Backup;
import com.bcm.shared.model.database.BackupState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class BackupStorageService {

    private final TaskRepository.BackupMapper backupMapper;

    public BackupStorageService(TaskRepository.BackupMapper backupMapper) {
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
        return new BackupDTO(
                backup.getId(),
                backup.getClientId(),
                backup.getTaskId(),
                "Backup-" + backup.getTaskId(),
                backup.getState(),
                backup.getSizeBytes(),
                toLdt(backup.getStartTime()),
                toLdt(backup.getStopTime()),
                toLdt(backup.getCreatedAt()),
                dto.getReplicationNodes()
        );

    }

    public static LocalDateTime toLdt(Instant t) {
        return t == null ? null : LocalDateTime.ofInstant(t, ZoneOffset.UTC);
    }

    public List<BackupDTO> findAllBackupsAsDto() {
        return backupMapper.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
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
