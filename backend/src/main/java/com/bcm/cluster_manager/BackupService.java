package com.bcm.cluster_manager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bcm.shared.model.BackupDTO;

@Service
public class BackupService {
     public List<BackupDTO> getAllBackups() {
        // Mock data for backups
        return Arrays.asList(
                new BackupDTO(1L, "Backup A", "Active", LocalDateTime.now().minusDays(1)),
                new BackupDTO(2L, "Backup B", "Inactive", LocalDateTime.now().minusDays(2)),
                new BackupDTO(3L, "Backup C", "Active", LocalDateTime.now().minusDays(3))
        );
    }
}
