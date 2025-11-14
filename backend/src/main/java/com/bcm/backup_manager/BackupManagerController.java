package com.bcm.backup_manager;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BackupManagerController {

    @GetMapping("/backupManager/test")
    public String test(){
        return "This is a backup manager endpoint";
    }
}