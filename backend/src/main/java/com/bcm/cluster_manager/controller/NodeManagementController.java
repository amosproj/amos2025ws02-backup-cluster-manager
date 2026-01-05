package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.NodeManagementService;
import com.bcm.shared.config.permissions.Permission;
import com.bcm.shared.model.api.NodeControlResponse;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.RegisterRequest;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


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

    @PreAuthorize(Permission.Require.NODE_READ)
    @GetMapping("/nodes/{id}")
    public ResponseEntity<NodeDTO> getNodeById(@PathVariable Long id) {
        Optional<NodeDTO> node = nodeManagementService.getNodeById(id);
        return node.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest req) {
        nodeManagementService.registerNode(req);
    }

    @PreAuthorize(Permission.Require.NODE_CONTROL)
    @PostMapping("/nodes/{id}/shutdown")
    public ResponseEntity<NodeControlResponse> shutdownNode(@PathVariable Long id) {
        boolean success = nodeManagementService.shutdownNode(id);
        if (success) {
            return ResponseEntity.ok(NodeControlResponse.success("Shutdown command sent successfully"));
        }
        return ResponseEntity.badRequest().body(NodeControlResponse.error("Failed to send shutdown command"));
    }

    @PreAuthorize(Permission.Require.NODE_CONTROL)
    @PostMapping("/nodes/{id}/restart")
    public ResponseEntity<NodeControlResponse> restartNode(@PathVariable Long id) {
        boolean success = nodeManagementService.restartNode(id);
        if (success) {
            return ResponseEntity.ok(NodeControlResponse.success("Restart command sent successfully"));
        }
        return ResponseEntity.badRequest().body(NodeControlResponse.error("Failed to send restart command"));
    }

    @PreAuthorize(Permission.Require.NODE_DELETE)
    @DeleteMapping("/nodes/{id}")
    public ResponseEntity<NodeControlResponse> removeNode(@PathVariable Long id) {
        boolean success = nodeManagementService.removeNode(id);
        if (success) {
            return ResponseEntity.ok(NodeControlResponse.success("Node removed from cluster successfully"));
        }
        return ResponseEntity.badRequest().body(NodeControlResponse.error("Failed to remove node from cluster"));
    }
}
