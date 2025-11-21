package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.database.Client;
import com.bcm.cluster_manager.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    public Client getClientById(Long id) {
        return clientRepository.findById(id);
    }

    @Transactional
    public Client getClientByNameOrIp(String nameOrIp) {
        return clientRepository.findByNameOrIp(nameOrIp);
    }

    @Transactional
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Transactional
    public Client addClient(Client client) {
        clientRepository.insert(client);
        return clientRepository.findById(client.getId());
    }

    @Transactional
    public Client editClient(Client client) {
        clientRepository.update(client);
        return clientRepository.findById(client.getId());
    }

    @Transactional
    public boolean deleteClient(Long id) {
        return clientRepository.delete(id) == 1;
    }
}
