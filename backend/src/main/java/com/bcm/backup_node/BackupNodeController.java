package com.bcm.backup_node;


import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.service.BackupStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class BackupNodeController {

    @Autowired
    private BackupStorageService backupStorageService;

    @Autowired
    private BackupNodeService backupNodeService;

    public BackupNodeController(BackupNodeService backupNodeService) {
        this.backupNodeService = backupNodeService;
    }
    @GetMapping("/backups")
    public List<BackupDTO> getBackups() {
        return backupStorageService.findAllBackupsAsDto();
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
