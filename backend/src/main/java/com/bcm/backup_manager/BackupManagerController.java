package com.bcm.backup_manager;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.BackupDeleteDTO;
import com.bcm.shared.service.BackupStorageService;
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

    @GetMapping("/backups")
    public ResponseEntity<Iterable<BackupDTO>> getBackups() {
        Iterable<BackupDTO> backups = backupStorageService.findAllBackupsAsDto();
        return ResponseEntity.ok(backups);
    }

    @PostMapping("/backups/delete")
    public ResponseEntity<Void> deleteBackup(@RequestBody BackupDeleteDTO request) {
        backupManagerService.deleteBackup(request.getBackupId(), request.getNodeAddresses());
        return ResponseEntity.noContent().build();
    }


}