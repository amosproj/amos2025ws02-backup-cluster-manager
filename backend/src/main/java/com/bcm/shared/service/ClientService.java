package com.bcm.shared.service;

import com.bcm.shared.model.database.Client;
import com.bcm.shared.repository.ClientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ClientService {

    private final ClientMapper clientMapper;

    public ClientService( ClientMapper clientMapper) {
        this.clientMapper = clientMapper;
    }

    @Transactional
    public Mono<Client> getClientById(Long id) {
        return clientMapper.findById(id);
    }

    @Transactional
    public Mono<Client> getClientByNameOrIp(String nameOrIp) {
        return clientMapper.findByNameOrIp(nameOrIp);
    }

    @Transactional
    public Mono<List<Client>> getAllClients() {
        return clientMapper.findAll().collectList();
    }

    public Mono<Client> addClient(Client client) {
        return clientMapper.save(client);
    }

    public Mono<Client> editClient(Client client) {
        return clientMapper.save(client);
    }

    public Mono<Void> deleteClient(Long id) {
        return clientMapper.deleteById(id);
    }
}
