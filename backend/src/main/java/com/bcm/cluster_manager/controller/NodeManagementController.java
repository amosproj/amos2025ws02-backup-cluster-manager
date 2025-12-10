package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.NodeManagementService;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.RegisterRequest;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController()
@RequestMapping("/api/v1/cm")
public class NodeManagementController {

    @Autowired
    private NodeManagementService nodeManagementService;

    @GetMapping("/nodes")
    public PaginationResponse<NodeDTO> getNodes(PaginationRequest pagination) {
        return nodeManagementService.getPaginatedItems(pagination);
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest req) {
        NodeDTO nodeDto = new NodeDTO(null, req.getAddress(), req.getAddress(), null, req.getMode(), null);
        nodeManagementService.registerNode(nodeDto);
    }
}
