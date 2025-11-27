package com.bcm.backup_manager;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.cluster_manager.model.api.BackupDeleteDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class BackupManagerController {

    private final BackupManagerService backupManagerService;

    public BackupManagerController(BackupManagerService backupManagerService) {
        this.backupManagerService = backupManagerService;
    }

    @PostMapping("/backups")
    public ResponseEntity<Void> createBackup(@RequestBody BackupDTO dto) {
        backupManagerService.distributeBackup(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/backups/delete")
    public ResponseEntity<Void> deleteBackup(@RequestBody BackupDeleteDTO request) {
        backupManagerService.deleteBackup(request.getBackupId(), request.getNodeAddresses());
        return ResponseEntity.noContent().build();
    }


}