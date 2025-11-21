package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.model.api.ClientDTO;
import com.bcm.cluster_manager.model.database.Client;
import com.bcm.cluster_manager.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/v1")
public class ClientController {

    @Autowired
    ClientService clientService;

    @GetMapping("/tasks")
    public List<ClientDTO> getTasks() {
        return clientService.getAllClients().stream()
                .map(this::toDto)
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
