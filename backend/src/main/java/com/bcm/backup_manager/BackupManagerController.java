package com.bcm.backup_manager;

import com.bcm.shared.model.api.BackupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backup-manager")
@CrossOrigin(originPatterns = "*")
@Profile("backup_manager")
public class BackupManagerController {

    @Autowired
    private BackupManagerService backupManagerService;

    @PostMapping("/backups")
    public ResponseEntity<Void> createBackup(@RequestBody BackupDTO dto) {
        backupManagerService.distributeBackup(dto);
        return ResponseEntity.ok().build();
    }
}