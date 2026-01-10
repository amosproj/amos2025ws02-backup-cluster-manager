package com.bcm.shared.controller;

import com.bcm.shared.model.api.ClientDTO;
import com.bcm.shared.model.database.Client;
import com.bcm.shared.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/bn")
public class ClientController {

    @Autowired
    ClientService clientService;

    @GetMapping("/clients")
    public Mono<List<ClientDTO>> getClients() {
        return clientService.getAllClients()
                .map(clients -> clients.stream()
                        .map(this::toDto)
                        .toList()
                );
    }

    private ClientDTO toDto(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setNameOrIp(client.getNameOrIp());
        dto.setEnabled(client.isEnabled());
        return dto;
    }
}
