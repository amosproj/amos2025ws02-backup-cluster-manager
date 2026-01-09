package com.bcm.cluster_manager.controller;

import com.bcm.shared.config.permissions.Permission;
import com.bcm.cluster_manager.model.api.BigBackupDTO;
import com.bcm.cluster_manager.service.CMBackupService;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController()
@RequestMapping("/api/v1/cm")
public class CMBackupController {


    @Autowired
    private CMBackupService CMBackupService;


    @PreAuthorize(Permission.Require.BACKUP_DELETE)
    @DeleteMapping("/backups/{id}")
    public Mono<ResponseEntity<Void>> deleteBackup(@PathVariable Long id, @RequestParam("nodeAddress") String nodeAddress) {
        return Mono.fromRunnable(() -> CMBackupService.deleteBackup(id, nodeAddress))
                .thenReturn(ResponseEntity.noContent().<Void>build())
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Void>build());
                });
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
    public Mono<PaginationResponse<BigBackupDTO>> getBackups(PaginationRequest pagination) {
        return Mono.fromSupplier(() -> CMBackupService.getPaginatedItems(pagination));
    }

    @PreAuthorize(Permission.Require.BACKUP_CREATE)
    @PostMapping("/backups")
    public Mono<ResponseEntity<BigBackupDTO>> createBackup(@RequestBody BigBackupDTO request) {
        return Mono.fromSupplier(() -> CMBackupService.createBackup(request))
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result))
                .onErrorResume(e -> {;
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

}
