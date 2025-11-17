package com.bcm.shared.service;

import com.bcm.shared.model.database.Backup;
import com.bcm.shared.repository.BackupMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BackupMService {

    private final BackupMapper backupMapper;

    public BackupMService(BackupMapper backupMapper) {
        this.backupMapper = backupMapper;
    }

    @Transactional
    public Backup getBackupById(Long id) {
        return backupMapper.findById(id);
    }

    @Transactional
    public List<Backup> getBackupsOfClient(Long clientId) {
        return backupMapper.findByClient(clientId);
    }

    @Transactional
    public List<Backup> getBackupsWithState(String state) {
        return backupMapper.findByState(state);
    }

    @Transactional
    public List<Backup> getBackupsInTimeInterval(Instant start, Instant end) {
        return backupMapper.findBetween(start, end);
    }

    @Transactional
    public Backup addBackup(Backup backup) {
        backupMapper.insert(backup);
        return backupMapper.findById(backup.getId());
    }

    @Transactional
    public Backup editBackup(Backup backup) {
        backupMapper.update(backup);
        return backupMapper.findById(backup.getId());
    }

    @Transactional
    public boolean deleteBackup(Long id) {
        return backupMapper.delete(id) == 1;
    }
}
