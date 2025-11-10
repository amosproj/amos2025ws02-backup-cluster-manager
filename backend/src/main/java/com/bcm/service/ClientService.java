package com.bcm.service;

import com.bcm.domain.Client;
import com.bcm.repository.ClientRepository;
import com.bcm.service.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClientService {

    private final ClientRepository clients;

    public ClientService(ClientRepository clients) {
        this.clients = clients;
    }

    public Client create(Client c) {
        if (c.getId() == null) c.setId(UUID.randomUUID());
        return clients.save(c);
    }

    @Transactional(readOnly = true)
    public Client get(UUID id) {
        return clients.findById(id).orElseThrow(() ->
                new NotFoundException("Client %s not found".formatted(id)));
    }

    @Transactional(readOnly = true)
    public List<Client> listAll() { return clients.findAll(); }

    public Client update(UUID id, Client patch) {
        Client c = get(id);
        if (patch.getNameOrIp() != null) c.setNameOrIp(patch.getNameOrIp());
        c.setEnabled(patch.isEnabled());
        return clients.save(c);
    }

    public void delete(UUID id) { clients.deleteById(id); }
}
