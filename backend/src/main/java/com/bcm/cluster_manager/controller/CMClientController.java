package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.model.api.BigClientDTO;
import com.bcm.cluster_manager.service.CMClientService;
import com.bcm.shared.model.api.ClientDTO;
import com.bcm.shared.model.database.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/cm")
public class CMClientController {

    @Autowired
    CMClientService clientService;

    @GetMapping("/clients")
    public List<BigClientDTO> getClients() {
        return clientService.getAllClients().stream()
                .toList();
    }

    private ClientDTO toDto(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setNameOrIp(client.getNameOrIp());
        dto.setEnabled(client.isEnabled());
        return dto;
    }
}
