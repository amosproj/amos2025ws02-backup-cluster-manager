package com.bcm.backup_manager;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backupManager")
public class BackupManagerController {

    @GetMapping("/test")
    public String test(){
        return "This is a backup manager endpoint";
    }
}
