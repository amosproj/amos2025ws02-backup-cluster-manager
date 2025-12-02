package com.bcm.backup_node.service;

import com.bcm.backup_node.model.database.BackupData;
import com.bcm.backup_node.repository.BackupDataMapper;
import com.bcm.shared.model.api.ExecuteBackupRequest;
import com.bcm.shared.model.api.BackupDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class BackupNodeService {

    private final BackupDataMapper backupDataMapper;

    public BackupNodeService(BackupDataMapper backupDataMapper) {
        this.backupDataMapper = backupDataMapper;
    }

    public void storeBackup(BackupDTO dto) {
        BackupData data = new BackupData();
        data.setId(dto.getId());
        data.setBackup_data("{\"mock\": \"backup content\"}");
        data.setCreatedAt(Instant.now());
        backupDataMapper.insert(data);
    }

    @Transactional
    public void deleteBackupData(Long backupId) {
        backupDataMapper.delete(backupId);
    }


    public void executeBackupSync(Long id, ExecuteBackupRequest request) {
        // Mock implementation: In a real scenario, this would trigger the backup process.
        // wait request.getDuration() and then return
        long now = System.currentTimeMillis();

         try {
            Thread.sleep(request.getDuration());

            BackupData data = backupDataMapper.findById(id);
            data.setBackup_data("{\"mock\": \"backup content updated " + "\"durationMs\": " + request.getDuration() + "," + "\"updatedAt\": " + now + "\"}");
            backupDataMapper.update(data);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }
}
