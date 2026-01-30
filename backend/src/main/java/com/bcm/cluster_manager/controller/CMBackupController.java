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

/**
 * REST controller for cluster manager backups: list, create, delete backups across nodes.
 */
@RestController()
@RequestMapping("/api/v1/cm")
public class CMBackupController {


    @Autowired
    private CMBackupService CMBackupService;


    /**
     * Deletes a backup on the given node by id.
     *
     * @param id           backup id
     * @param nodeAddress  node address where the backup resides
     * @return 204 on success, 500 on error
     */
    @PreAuthorize(Permission.Require.BACKUP_DELETE)
    @DeleteMapping("/backups/{id}")
    public Mono<ResponseEntity<Void>> deleteBackup(@PathVariable Long id, @RequestParam("nodeAddress") String nodeAddress) {
        return CMBackupService.deleteBackup(id, nodeAddress)
                .thenReturn(ResponseEntity.noContent().<Void>build())
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Void>build());
                });
    }

    /**
     * Returns a paginated list of backups across all nodes.
     *
     * @param pagination pagination and filter parameters
     * @return paginated response of backup DTOs
     */
    @PreAuthorize(Permission.Require.BACKUP_READ)
    @GetMapping("/backups")
    public Mono<PaginationResponse<BigBackupDTO>> getBackups(PaginationRequest pagination) {
        return CMBackupService.getPaginatedItems(pagination);
    }

    /**
     * Creates a new backup on the target node.
     *
     * @param request backup DTO with node and backup details
     * @return 201 with created backup, 500 on error
     */
    @PreAuthorize(Permission.Require.BACKUP_CREATE)
    @PostMapping("/backups")
    public Mono<ResponseEntity<BigBackupDTO>> createBackup(@RequestBody BigBackupDTO request) {
        return CMBackupService.createBackup(request)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result))
                .onErrorResume(e -> {;
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

}
