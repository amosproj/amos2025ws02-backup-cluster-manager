package com.bcm.backup_manager;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.cluster_manager.service.BackupStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class BackupManagerController {

    private final BackupStorageService backupStorageService;
    private final BackupManagerService backupManagerService;

    public BackupManagerController(BackupStorageService backupStorageService,
                                   BackupManagerService backupManagerService) {
        this.backupStorageService = backupStorageService;
        this.backupManagerService = backupManagerService;
    }


    @PostMapping("/backups")
    public ResponseEntity<Void> createBackup(@RequestBody BackupDTO dto) {
        backupManagerService.distributeBackup(dto);
        return ResponseEntity.ok().build();
    }

}