package com.bcm.backup_node;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.database.Backup;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.repository.BackupMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BackupNodeService {

    private final BackupMapper backupMapper;

    public BackupNodeService(BackupMapper backupMapper) {
        this.backupMapper = backupMapper;
    }

    public void storeBackup(BackupDTO dto) {
        Backup backup = new Backup();
        backup.setClientId(dto.getClientId());
        backup.setTaskId(dto.getTaskId());
        backup.setSizeBytes(dto.getSizeBytes() != null ? dto.getSizeBytes() : 0L);
        backup.setStartTime(Instant.now());
        backup.setState(BackupState.RUNNING);
        backup.setMessage(null);
        backup.setCreatedAt(Instant.now());

        backupMapper.insert(backup);
    }
}
