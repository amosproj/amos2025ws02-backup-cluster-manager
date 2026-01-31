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


/**
 * REST controller for cluster manager node management: list, register, update, delete, and control nodes.
 */
@RestController()
@RequestMapping("/api/v1/cm")
public class NodeManagementController {

    @Autowired
    private NodeManagementService nodeManagementService;

    /**
     * Returns a paginated list of registered nodes.
     *
     * @param pagination pagination and filter parameters
     * @return paginated response of node DTOs
     */
    @PreAuthorize(Permission.Require.NODE_READ)
    @GetMapping("/nodes")
    public Mono<PaginationResponse<NodeDTO>> getNodes(PaginationRequest pagination) {
        return nodeManagementService.getPaginatedItems(pagination);
    }

    /**
     * Updates a node's managed mode.
     *
     * @param nodeDTO node DTO containing id and managed mode
     * @return completion when update is done
     */
    @PreAuthorize(Permission.Require.NODE_UPDATE)
    @PutMapping("/node")
    public Mono<Void> updateManageMode(@RequestBody NodeDTO nodeDTO) {
        return Mono.fromRunnable(() -> nodeManagementService.updateNodeManagedMode(nodeDTO));
    }

    /**
     * Deletes a registered node by id.
     *
     * @param id node id
     * @return completion when delete is done
     */
    @PreAuthorize(Permission.Require.NODE_DELETE)
    @DeleteMapping("/node/{id}")
    public Mono<Void> deleteNode(@PathVariable Long id) {
        return nodeManagementService.deleteNode(id);
    }

    /**
     * Returns a single node by id.
     *
     * @param id node id
     * @return 200 with node DTO, or 404 if not found
     */
    @PreAuthorize(Permission.Require.NODE_READ)
    @GetMapping("/nodes/{id}")
    public Mono<ResponseEntity<NodeDTO>> getNodeById(@PathVariable Long id) {
        return Mono.fromCallable(() -> nodeManagementService.getNodeById(id))
                .map(optNode -> optNode.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()));
    }

    /**
     * Registers a backup node with the cluster manager.
     *
     * @param req registration request (URL, managed flag, etc.)
     * @return 200 with status, or 400 with error message on failure
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<Map<String, String>>> register(@RequestBody RegisterRequest req) {
        if (req.getIsManaged() == null){
            req.setIsManaged(false);
        }
        return nodeManagementService.registerNode(req)
                .thenReturn(ResponseEntity.ok(Collections.singletonMap("status", "OK")))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()))));
    }

    /**
     * Sends a shutdown command to the given node.
     *
     * @param id node id
     * @return 200 with success message, or 400 on failure
     */
    @PreAuthorize(Permission.Require.NODE_CONTROL)
    @PostMapping("/nodes/{id}/shutdown")
    public Mono<ResponseEntity<NodeControlResponse>> shutdownNode(@PathVariable Long id) {
        return nodeManagementService.shutdownNode(id)
                .map(success -> {
                    if (success) {
                        return ResponseEntity.ok(NodeControlResponse.success("Shutdown command sent successfully"));
                    }
                    return ResponseEntity.badRequest().body(NodeControlResponse.error("Failed to send shutdown command"));
                });
    }

    /**
     * Sends a restart command to the given node.
     *
     * @param id node id
     * @return 200 with success message, or 400 on failure
     */
    @PreAuthorize(Permission.Require.NODE_CONTROL)
    @PostMapping("/nodes/{id}/restart")
    public Mono<ResponseEntity<NodeControlResponse>> restartNode(@PathVariable Long id) {
        return nodeManagementService.restartNode(id)
                .map(success -> {
                    if (success) {
                        return ResponseEntity.ok(NodeControlResponse.success("Restart command sent successfully"));
                    }
                    return ResponseEntity.badRequest().body(NodeControlResponse.error("Failed to send restart command"));
                });
    }
}
