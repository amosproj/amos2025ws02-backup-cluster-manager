package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.model.api.CreateBackupRequest;
import com.bcm.cluster_manager.service.BackupService;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/v1")
public class BackupController {


    @Autowired
    private BackupService backupService;


    @GetMapping("/backups")
    public PaginationResponse<BackupDTO> getBackups(PaginationRequest pagination) {
        return backupService.getPaginatedItems(pagination);
    }

    @PostMapping("/backups")
    public ResponseEntity<BackupDTO> createBackup(@RequestBody CreateBackupRequest request) {
        try {

            BackupDTO result = backupService.createBackup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
