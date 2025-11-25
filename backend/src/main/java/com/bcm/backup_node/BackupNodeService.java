package com.bcm.backup_node;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.service.BackupDataStorageService;
import org.springframework.stereotype.Service;

@Service
public class BackupNodeService {

    private final BackupDataStorageService storage;

    public BackupNodeService(BackupDataStorageService storage) {
        this.storage = storage;
    }

    public void storeBackup(BackupDTO dto) {
        storage.storeBackupData(dto);
    }
}
