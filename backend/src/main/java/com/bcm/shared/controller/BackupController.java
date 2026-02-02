package com.bcm.shared.controller;


import com.bcm.cluster_manager.service.CMBackupService;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.ExecuteBackupRequest;
import com.bcm.shared.service.BackupService;
import com.bcm.shared.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller for backup node backups: list, delete, sync, and execute backups.
 */
@RestController
@RequestMapping("/api/v1/bn")
public class BackupController {

    @Autowired
    private BackupService backupNodeService;

    @Autowired
    private ClientService clientService;

    private static final Logger logger = LoggerFactory.getLogger(BackupController.class);


    /**
     * Deletes a backup by id.
     *
     * @param id backup id
     * @return 204 on success, 500 on error
     */
    @DeleteMapping("/backups/{id}")
    public Mono<ResponseEntity<Void>> deleteBackup(@PathVariable Long id) {
        return backupNodeService.deleteBackup(id)
                .thenReturn(ResponseEntity.noContent().<Void>build())
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * Returns all backups.
     *
     * @return 200 with list of backup DTOs, or 500 on error
     */
    @GetMapping("/backups")
    public Mono<ResponseEntity<List<BackupDTO>>> getBackups() {
        return backupNodeService.getAllBackups()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * Simple test endpoint for backup node.
     *
     * @return test message
     */
    @GetMapping("/backups/test")
    public String test(){
        return "This is a backup node endpoint";
    }

    /**
     * Receives a backup sync from the cluster manager (store backup record).
     *
     * @param dto backup DTO with client id, task id, size
     * @return 200 with stored backup, 400 if client not found, 500 on error
     */
    @PostMapping("/backups/sync")
    public Mono<ResponseEntity<BackupDTO>> receiveBackup(@RequestBody BackupDTO dto) {
        // TODO:
        if (clientService.getClientById(dto.getClientId()) == null) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return backupNodeService.store(dto.getClientId(), dto.getTaskId(), dto.getSizeBytes())
                .map(ResponseEntity::ok)
                .onErrorResume(e ->{
                        logger.error("Error in sync for client {}: {}", dto.getClientId(), e.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                        }
                );
    }


    /**
     * Triggers execution of a backup by id.
     *
     * @param id      backup id
     * @param request execute backup request
     * @return 200 on success, 500 on error
     */
    @PostMapping("/backups/{id}/execute")
    public Mono<ResponseEntity<Void>> executeBackup(@PathVariable Long id,
                                                    @RequestBody ExecuteBackupRequest request) {
        return backupNodeService.findBackupById(id)
                .flatMap(found -> backupNodeService.executeBackupSync(id, request)
                        .thenReturn(ResponseEntity.ok().<Void>build()))
                .switchIfEmpty(Mono.just(ResponseEntity.ok().build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}
