package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.NodeManagementService;
import com.bcm.cluster_manager.service.RegistryService;
import com.bcm.cluster_manager.service.SyncService;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.RegisterRequest;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController()
@RequestMapping("/api/v1/cm")
public class ClusterManagerController {

    @Autowired
    private NodeManagementService nodeManagementService;

    @Autowired
    private RegistryService registry;

    @Autowired
    private SyncService syncService;


    @GetMapping("/nodes")
    public PaginationResponse<NodeDTO> getNodes(PaginationRequest pagination) {
        return nodeManagementService.getPaginatedItems(pagination);
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest req) {
        registry.register(req.getAddress());
        // push updated tables to all nodes
        syncService.pushTablesToAllNodes();
    }
}
