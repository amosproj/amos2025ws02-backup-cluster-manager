package com.bcm.cluster_manager.controller;

import com.bcm.shared.config.permissions.Permission;
import com.bcm.shared.model.api.CreateBackupRequest;
import com.bcm.cluster_manager.model.api.BigBackupDTO;
import com.bcm.cluster_manager.service.CMBackupService;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/v1/cm")
public class CMBackupController {


    @Autowired
    private CMBackupService CMBackupService;


    @PreAuthorize(Permission.Require.BACKUP_DELETE)
    @DeleteMapping("/backups/{id}")
    public ResponseEntity<Void> deleteBackup(@PathVariable Long id, @RequestParam("nodeAddress") String nodeAddress) {
        try {
            CMBackupService.deleteBackup(id, nodeAddress);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*
    @PostMapping("/backups/{id}/execute")
    public ResponseEntity<Void> executeBackup(@PathVariable Long id, @RequestBody ExecuteBackupRequest req) {
        try {
            CMBackupService.executeBackup(id, req.getDuration(), req.getShouldSucceed());
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    */

    @PreAuthorize(Permission.Require.BACKUP_READ)
    @GetMapping("/backups")
    public PaginationResponse<BigBackupDTO> getBackups(PaginationRequest pagination) {
        return CMBackupService.getPaginatedItems(pagination);
    }

    @PreAuthorize(Permission.Require.BACKUP_CREATE)
    @PostMapping("/backups")
    public ResponseEntity<BigBackupDTO> createBackup(@RequestBody BigBackupDTO request) {
        try {

            BigBackupDTO result = CMBackupService.createBackup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
