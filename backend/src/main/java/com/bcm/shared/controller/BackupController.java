package com.bcm.shared.controller;


import com.bcm.shared.service.BackupNodeService;
import com.bcm.shared.model.api.ExecuteBackupRequest;
import com.bcm.shared.model.api.BackupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bn")
public class BackupController {

    @Autowired
    private BackupNodeService backupNodeService;

    public BackupController(BackupNodeService backupNodeService) {
        this.backupNodeService = backupNodeService;
    }

    @DeleteMapping("/backups/{id}")
    public void deleteBackup(@PathVariable Long id) {
        backupNodeService.deleteBackupData(id);
    }


    @DeleteMapping("/backups/{id}")
    public ResponseEntity<Void> deleteBackup(@PathVariable Long id) {
        try {
            backupService.deleteBackup(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/backupNode/test")
    public String test(){
        return "This is a backup node endpoint";
    }

    @PostMapping("/backups/sync")
    public void receiveBackup(@RequestBody BackupDTO dto) {
        backupNodeService.storeBackup(dto);
    }

    @PostMapping("/backups/{id}/execute")
    public ResponseEntity<Void> executeBackup(
            @PathVariable Long id,
            @RequestBody ExecuteBackupRequest request
    ) {
        try {
            backupNodeService.executeBackupSync(id, request);

            // return 200
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            // return 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
