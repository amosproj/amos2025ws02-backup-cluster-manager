package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.ClusterManagerService;
import com.bcm.cluster_manager.service.BackupService;
import com.bcm.cluster_manager.service.RegistryService;
import com.bcm.cluster_manager.service.SyncService;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.RegisterRequest;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public PaginationResponse<NodeDTO> getNodes(PaginationRequest pagination) {
        return clusterManagerService.getPaginatedItems(pagination);
    }

    @GetMapping("/backups")
    public PaginationResponse<BackupDTO> getBackups(PaginationRequest pagination) {
        return backupService.getPaginatedItems(pagination);
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest req) {
        registry.register(req.getAddress());
        // push updated tables to all nodes
        syncService.pushTablesToAllNodes();
    }
}
