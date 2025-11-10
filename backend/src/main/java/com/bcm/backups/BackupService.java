package com.bcm.backups;

import api.model.BackupClass;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class BackupService {

    public List<BackupClass> getAllBackups() {
        // Mock data for backups
        return Arrays.asList(
            new BackupClass(1L, "Backup A", "Active", LocalDateTime.now().minusDays(1)),
            new BackupClass(2L, "Backup B", "Inactive", LocalDateTime.now().minusDays(2)),
            new BackupClass(3L, "Backup C", "Active", LocalDateTime.now().minusDays(3))
        );
    }
}