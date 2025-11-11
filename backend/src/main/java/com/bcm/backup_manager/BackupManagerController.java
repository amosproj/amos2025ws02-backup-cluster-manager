package com.bcm.backup_manager;


import java.util.List;

import com.bcm.shared.model.BackupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/backupManager")
public class BackupManagerController {

    @GetMapping("/test")
    public String test(){
        return "This is a backup manager endpoint";
    }
     @Autowired
    private BackupManagerService backupManagerService;


    @GetMapping("/backups")
    public List<BackupDTO> getBackups() {
        return backupManagerService.getAllBackups();
    }
}
