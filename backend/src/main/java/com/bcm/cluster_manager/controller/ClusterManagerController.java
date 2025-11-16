package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.ClusterManagerService;
import com.bcm.cluster_manager.service.BackupService;
import com.bcm.cluster_manager.service.RegistryService;
import com.bcm.cluster_manager.service.SyncService;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.RegisterRequest;
import com.bcm.shared.model.database.Group;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import com.bcm.shared.service.GroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bcm.cluster_manager.dto.CreateBackupRequest;

@RestController()
@RequestMapping("/api/v1")
public class ClusterManagerController {

    @Autowired
    private ClusterManagerService clusterManagerService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private BackupService backupService;

    @Autowired
    private RegistryService registry;

    @Autowired
    private SyncService syncService;

    @GetMapping("/nodes")
    public PaginationResponse<NodeDTO> getNodes(PaginationRequest pagination) {
        return clusterManagerService.getPaginatedItems(pagination);
    }

    @GetMapping("/groups")
    public List<Group> getGroups() {
        return groupService.getAllGroups();
    }

    @GetMapping("/backups")
    public PaginationResponse<BackupDTO> getBackups(PaginationRequest pagination) {
            return backupService.getPaginatedItems(pagination);
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
