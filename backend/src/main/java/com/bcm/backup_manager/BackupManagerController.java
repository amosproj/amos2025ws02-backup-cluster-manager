package com.bcm.backup_manager;

import com.bcm.shared.model.api.BackupDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class BackupManagerController {

    private final BackupManagerService backupManagerService;

    public BackupManagerController(BackupManagerService backupManagerService) {
        this.backupManagerService = backupManagerService;
    }


    @PostMapping("/backupsData")
    public ResponseEntity<Void> createBackupWithData(@RequestBody BackupDTO dto) {
        backupManagerService.distributeBackup(dto);
        return ResponseEntity.ok().build();
    }

}