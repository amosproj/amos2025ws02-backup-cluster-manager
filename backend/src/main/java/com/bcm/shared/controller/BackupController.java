package com.bcm.shared.controller;


import com.bcm.shared.model.api.CreateBackupRequest;
import com.bcm.shared.model.api.ExecuteBackupRequest;
import com.bcm.shared.service.BackupService;
import com.bcm.shared.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bn")
public class BackupController {

    @Autowired
    private BackupService backupNodeService;

    @Autowired
    private ClientService clientService;


    @DeleteMapping("/backups/{id}")
    public ResponseEntity<Void> deleteBackup(@PathVariable Long id) {
        try {
            backupNodeService.deleteBackup(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/backups")
    public ResponseEntity<?> getBackups() {
        try {
            return ResponseEntity.ok(backupNodeService.getAllBackups());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/backups/test")
    public String test(){
        return "This is a backup node endpoint";
    }

    @PostMapping("/backups/sync")
    public void receiveBackup(@RequestBody CreateBackupRequest dto) {

        // get all clients and check if dto client id is in the list
        if ( clientService.getClientById(dto.getClientId()) != null) {

            backupNodeService.store(dto.getClientId(), dto.getTaskId(), dto.getSizeBytes());

        } else {
            System.out.println("Received backup for unknown client id: " + dto.getClientId());
        }
    }

    @PostMapping("/backups/{id}/execute")
    public ResponseEntity<Void> executeBackup(
            @PathVariable Long id,
            @RequestBody ExecuteBackupRequest request
    ) {
        try {
            if (backupNodeService.findBackupById(id) != null) {
                backupNodeService.executeBackupSync(id, request);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            // return 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
