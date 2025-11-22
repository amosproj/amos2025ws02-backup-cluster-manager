package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.ClusterManagerService;
import com.bcm.cluster_manager.service.BackupService;
import com.bcm.cluster_manager.service.RegistryService;
import com.bcm.cluster_manager.service.SyncService;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bcm.cluster_manager.dto.CreateBackupRequest;
import java.util.ArrayList;
import java.util.List;

@RestController()
@RequestMapping("/api/v1")
public class ClusterManagerController {

    @Autowired
    private ClusterManagerService clusterManagerService;

    @Autowired
    private BackupService backupService;

    @Autowired
    private RegistryService registry;

    @Autowired
    private SyncService syncService;


    @GetMapping("/nodes")
    public List<NodeDTO> getNodes() {
        return clusterManagerService.getAllNodes();
    }

    @GetMapping("/backups")
    public ResponseEntity<List<BackupDTO>> getBackups() {
        try {
            List<BackupDTO> backups = clusterManagerService.getAllBackups();
            return ResponseEntity.ok(backups);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @PostMapping("/backups")
    public ResponseEntity<BackupDTO> createBackup(@RequestBody CreateBackupRequest request) {
        try {

            BackupDTO result = clusterManagerService.createBackup(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest req) {
        registry.register(req.getAddress());
        // push updated tables to all nodes
        syncService.pushTablesToAllNodes();
    }

}
