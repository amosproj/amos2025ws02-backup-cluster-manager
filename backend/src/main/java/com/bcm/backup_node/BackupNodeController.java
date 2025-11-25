package com.bcm.backup_node;


import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.service.BackupDataStorageService;
import com.bcm.shared.service.BackupStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class BackupNodeController {

    @Autowired
    private BackupDataStorageService backupStorageService;

    @Autowired
    private BackupNodeService backupNodeService;

    public BackupNodeController(BackupNodeService backupNodeService) {
        this.backupNodeService = backupNodeService;
    }

    @DeleteMapping("/backups/{id}")
    public void deleteBackup(@PathVariable Long id) {
        backupStorageService.deleteBackupData(id);
    }

    @GetMapping("/backupNode/test")
    public String test(){
        return "This is a backup node endpoint";
    }

    @PostMapping("/backups/sync")
    public void receiveBackup(@RequestBody BackupDTO dto) {
        backupNodeService.storeBackup(dto);
    }
}
