package com.bcm.backup_node.service;

import com.bcm.backup_node.model.database.BackupData;
import com.bcm.backup_node.repository.BackupDataMapper;
import com.bcm.shared.model.api.BackupDTO;
import org.springframework.stereotype.Service;

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
}
