package com.bcm.shared.service;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.database.BackupData;
import com.bcm.shared.repository.BackupDataMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class BackupDataStorageService {

    private final BackupDataMapper backupDataMapper;

    public BackupDataStorageService(BackupDataMapper backupDataMapper) {
        this.backupDataMapper = backupDataMapper;
    }

    @Transactional
    public void storeBackupData(BackupDTO dto) {
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


}
