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
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;


@RestController()
@RequestMapping("/api/v1/cm")
public class NodeManagementController {

    @Autowired
    private NodeManagementService nodeManagementService;

    @PreAuthorize(Permission.Require.NODE_READ)
    @GetMapping("/nodes")
    public Mono<PaginationResponse<NodeDTO>> getNodes(PaginationRequest pagination) {
        return nodeManagementService.getPaginatedItems(pagination);
    }

    @PreAuthorize(Permission.Require.NODE_UPDATE)
    @PutMapping("/node")
    public Mono<Void> updateManageMode(@RequestBody NodeDTO nodeDTO) {
        return Mono.fromRunnable(() -> nodeManagementService.updateNodeManagedMode(nodeDTO));
    }

    @PreAuthorize(Permission.Require.NODE_DELETE)
    @DeleteMapping("/node/{id}")
    public Mono<Void> deleteNode(@PathVariable Long id) {
        return Mono.fromRunnable(() -> nodeManagementService.deleteNode(id));
    }

    @PreAuthorize(Permission.Require.NODE_READ)
    @GetMapping("/nodes/{id}")
    public Mono<ResponseEntity<NodeDTO>> getNodeById(@PathVariable Long id) {
        return Mono.fromCallable(() -> nodeManagementService.getNodeById(id))
                .map(optNode -> optNode.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()));
    }

    @PreAuthorize(Permission.Require.NODE_CREATE)
    @PostMapping("/register")
    public Mono<ResponseEntity<Map<String, String>>> register(@RequestBody RegisterRequest req) {
        return Mono.fromCallable(() -> {
            nodeManagementService.registerNode(req);
            return ResponseEntity.ok(Collections.singletonMap("status", "OK"));
        }).onErrorResume(e ->
                Mono.just(ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid request"))));
    }

    @PreAuthorize(Permission.Require.NODE_CONTROL)
    @PostMapping("/nodes/{id}/shutdown")
    public Mono<ResponseEntity<NodeControlResponse>> shutdownNode(@PathVariable Long id) {
        return Mono.fromCallable(() -> nodeManagementService.shutdownNode(id))
                .map(success -> {
                    if (success) {
                        return ResponseEntity.ok(NodeControlResponse.success("Shutdown command sent successfully"));
                    }
                    return ResponseEntity.badRequest().body(NodeControlResponse.error("Failed to send shutdown command"));
                });
    }

    @PreAuthorize(Permission.Require.NODE_CONTROL)
    @PostMapping("/nodes/{id}/restart")
    public Mono<ResponseEntity<NodeControlResponse>> restartNode(@PathVariable Long id) {
        return Mono.fromCallable(() -> nodeManagementService.restartNode(id))
                .map(success -> {
                    if (success) {
                        return ResponseEntity.ok(NodeControlResponse.success("Restart command sent successfully"));
                    }
                    return ResponseEntity.badRequest().body(NodeControlResponse.error("Failed to send restart command"));
                });
    }
}
