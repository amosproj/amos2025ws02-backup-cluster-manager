package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.NodeManagementService;
import com.bcm.shared.config.permissions.Permission;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.RegisterRequest;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController()
@RequestMapping("/api/v1/cm")
public class NodeManagementController {

    @Autowired
    private NodeManagementService nodeManagementService;

    @PreAuthorize(Permission.Require.NODE_READ)
    @GetMapping("/nodes")
    public PaginationResponse<NodeDTO> getNodes(PaginationRequest pagination) {
        return nodeManagementService.getPaginatedItems(pagination);
    }

    @PreAuthorize(Permission.Require.NODE_UPDATE)
    @PutMapping("/node")
    public void updateManageMode(@RequestBody NodeDTO nodeDTO) {
        nodeManagementService.updateNodeManagedMode(nodeDTO);
    }

    @PreAuthorize(Permission.Require.NODE_DELETE)
    @DeleteMapping("/node/{id}")
    public void deleteNode(@PathVariable Long id) {
        nodeManagementService.deleteNode(id);
    }

    @PreAuthorize(Permission.Require.NODE_CREATE)
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest req) {
        try {
            nodeManagementService.registerNode(req);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid request");
        }
    }



}
