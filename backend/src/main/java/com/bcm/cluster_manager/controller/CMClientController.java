package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.model.api.BigClientDTO;
import com.bcm.cluster_manager.service.CMClientService;
import com.bcm.shared.config.permissions.Permission;
import com.bcm.shared.model.database.Client;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/cm")
public class CMClientController {

    @Autowired
    CMClientService CMclientService;

    @PreAuthorize(Permission.Require.CLIENT_READ)
    @GetMapping("/clients")
    public PaginationResponse<BigClientDTO> getClients(PaginationRequest pagination) {
        return CMclientService.getPaginatedItems(pagination);
    }

    @PreAuthorize(Permission.Require.CLIENT_READ)
    @GetMapping("/clientsList")
    public List<BigClientDTO> getClients() {
        return CMclientService.getAllClients().stream()
                .toList();
    }

    private BigClientDTO toDto(Client client) {
        BigClientDTO dto = new BigClientDTO();
        dto.setId(client.getId());
        dto.setNameOrIp(client.getNameOrIp());
        dto.setEnabled(client.isEnabled());
        return dto;
    }
}
