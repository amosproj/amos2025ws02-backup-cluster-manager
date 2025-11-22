package com.bcm.backup_node;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.service.BackupStorageService;
import org.springframework.stereotype.Service;

@Service
public class BackupNodeService {

    private final BackupStorageService storage;

    public BackupNodeService(BackupStorageService storage) {
        this.storage = storage;
    }

    public void storeBackup(BackupDTO dto) {
        storage.store(dto);
    }
}
