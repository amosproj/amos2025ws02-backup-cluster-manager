package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.model.api.BigClientDTO;
import com.bcm.cluster_manager.service.CMClientService;
import com.bcm.shared.config.permissions.Permission;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller for cluster manager clients: list clients across nodes.
 */
@RestController()
@RequestMapping("/api/v1/cm")
public class CMClientController {

    @Autowired
    CMClientService CMclientService;

    /**
     * Returns a paginated list of clients across all nodes.
     *
     * @param pagination pagination and filter parameters
     * @return paginated response of client DTOs
     */
    @PreAuthorize(Permission.Require.CLIENT_READ)
    @GetMapping("/clients")
    public Mono<PaginationResponse<BigClientDTO>> getClients(PaginationRequest pagination) {
        return CMclientService.getPaginatedItems(pagination);
    }

    /**
     * Returns all clients as a list (no pagination).
     *
     * @return list of client DTOs
     */
    @PreAuthorize(Permission.Require.CLIENT_READ)
    @GetMapping("/clientsList")
    public Mono<List<BigClientDTO>> getClientsList() {
        return CMclientService.getAllClients();
    }
}
