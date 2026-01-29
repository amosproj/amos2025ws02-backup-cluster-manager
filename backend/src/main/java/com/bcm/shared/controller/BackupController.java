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

@RestController
@RequestMapping("/api/v1/bn")
public class BackupController {

    @Autowired
    private BackupService backupNodeService;

    @Autowired
    private ClientService clientService;

    private static final Logger logger = LoggerFactory.getLogger(BackupController.class);


    @DeleteMapping("/backups/{id}")
    public Mono<ResponseEntity<Void>> deleteBackup(@PathVariable Long id) {
        return backupNodeService.deleteBackup(id)
                .thenReturn(ResponseEntity.noContent().<Void>build())
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @GetMapping("/backups")
    public Mono<ResponseEntity<List<BackupDTO>>> getBackups() {
        return backupNodeService.getAllBackups()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @GetMapping("/backups/test")
    public String test(){
        return "This is a backup node endpoint";
    }

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
