package com.bcm.backup_node.controller;


import com.bcm.backup_node.service.BackupNodeService;
import com.bcm.shared.model.api.BackupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class BackupNodeController {

    @Autowired
    private BackupNodeService backupNodeService;

    public BackupNodeController(BackupNodeService backupNodeService) {
        this.backupNodeService = backupNodeService;
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
