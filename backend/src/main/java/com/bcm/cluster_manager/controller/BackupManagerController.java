package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.BackupManagerService;
import com.bcm.shared.model.api.ExecuteBackupRequest;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.BackupDeleteDTO;
import com.bcm.shared.model.api.ExecuteBackupResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bm")
public class BackupManagerController {

    private final BackupManagerService backupManagerService;

    public BackupManagerController(BackupManagerService backupManagerService) {
        this.backupManagerService = backupManagerService;
    }

    @PostMapping("/backups")
    public ResponseEntity<Void> createBackup(@RequestBody BackupDTO dto) {
        backupManagerService.distributeBackup(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/backups/delete")
    public ResponseEntity<Void> deleteBackup(@RequestBody BackupDeleteDTO request) {
        backupManagerService.deleteBackup(request.getBackupId(), request.getNodeAddresses());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/backups/{id}/execute")
    public ResponseEntity<ExecuteBackupResponse> executeBackup(
            @PathVariable Long id,
            @RequestBody ExecuteBackupRequest request
    ) {
        try {
            // This blocks until all BNs complete
            boolean success = backupManagerService.execute(id, request);

            String message = success ? "Backup completed on all nodes" : "Backup failed on one or more nodes";

            return ResponseEntity.ok(new ExecuteBackupResponse(success, message));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ExecuteBackupResponse(false, "BM error: " + e.getMessage()));
        }
    }

}