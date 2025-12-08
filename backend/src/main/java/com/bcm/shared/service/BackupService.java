package com.bcm.shared.service;

import com.bcm.shared.model.api.*;
import com.bcm.shared.model.database.Backup;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.repository.BackupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bcm.shared.mapper.BackupConverter;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BackupService {

    @Autowired
    private BackupMapper backupMapper;


    public BackupDTO findBackupById(Long id) {
        return BackupConverter.toDTO(backupMapper.findById(id));
    }
    public List<BackupDTO> getAllBackups() {
        try {
            List<Backup> backups = backupMapper.findAll();
            if (backups == null || backups.isEmpty()) {
                return List.of();
            }
            return backups.stream()
                    .map(BackupConverter::toDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
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

            Backup backup = backupMapper.findById(id);
            if (request.getShouldSucceed()){
                backup.setState(BackupState.COMPLETED);
                backup.setMessage("Backup completed successfully.");
            } else {
                backup.setState(BackupState.FAILED);
                backup.setMessage("Backup failed due to an error.");
            }
            backup.setStopTime(Instant.ofEpochMilli(now + request.getDuration()));
            backupMapper.update(backup);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    public void store(Long clientId, Long taskId, Long sizeBytes) {
        Backup backup = new Backup();
        backup.setClientId(clientId);
        backup.setTaskId(taskId);
        backup.setSizeBytes(sizeBytes != null ? sizeBytes : 0L);
        backup.setStartTime(Instant.now());
        backup.setState(BackupState.QUEUED);
        backup.setMessage(null);
        backup.setCreatedAt(Instant.now());

        backupMapper.insert(backup);

    }

}
