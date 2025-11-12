package com.bcm.backup_node;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BackupNodeController {

    @GetMapping("/backupNode/test")
    public String test(){
        return "This is a backup node endpoint";
    }
}
