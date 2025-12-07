package com.bcm.shared.service;

import com.bcm.shared.model.api.*;
import com.bcm.shared.model.database.Backup;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.repository.BackupMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BackupService {

    private final BackupMapper backupMapper;

    public BackupService(BackupMapper backupMapper) {
        this.backupMapper = backupMapper;
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
