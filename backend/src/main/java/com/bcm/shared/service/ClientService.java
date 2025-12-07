package com.bcm.shared.service;

import com.bcm.shared.model.database.Client;
import com.bcm.shared.repository.ClientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientMapper clientMapper;


    @Transactional
    public Client getClientById(Long id) {
        return clientMapper.findById(id);
    }

    @Transactional
    public Client getClientByNameOrIp(String nameOrIp) {
        return clientMapper.findByNameOrIp(nameOrIp);
    }

    @Transactional
    public List<Client> getAllClients() {
        return clientMapper.findAll();
    }

    @Transactional
    public Client addClient(Client client) {
        clientMapper.insert(client);
        return clientMapper.findById(client.getId());
    }

    @Transactional
    public Client editClient(Client client) {
        clientMapper.update(client);
        return clientMapper.findById(client.getId());
    }

    @Transactional
    public boolean deleteClient(Long id) {
        return clientMapper.delete(id) == 1;
    }
}
