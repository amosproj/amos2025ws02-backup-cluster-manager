package com.bcm.shared.service;

import com.bcm.shared.model.database.Client;
import com.bcm.shared.repository.ClientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service for client CRUD operations on the backup node database.
 */
@Service
public class ClientService {

    private final ClientMapper clientMapper;

    /**
     * Creates the client service with the given client mapper.
     *
     * @param clientMapper client mapper
     */
    public ClientService( ClientMapper clientMapper) {
        this.clientMapper = clientMapper;
    }

    /**
     * Returns a client by id.
     *
     * @param id client id
     * @return the client, or empty if not found
     */
    @Transactional
    public Mono<Client> getClientById(Long id) {
        return clientMapper.findById(id);
    }

    /**
     * Returns a client by name or IP.
     *
     * @param nameOrIp name or IP
     * @return the client, or empty if not found
     */
    @Transactional
    public Mono<Client> getClientByNameOrIp(String nameOrIp) {
        return clientMapper.findByNameOrIp(nameOrIp);
    }

    /**
     * Returns all clients.
     *
     * @return list of clients
     */
    @Transactional
    public Mono<List<Client>> getAllClients() {
        return clientMapper.findAll().collectList();
    }

    /**
     * Adds a new client.
     *
     * @param client client to add
     * @return the saved client
     */
    public Mono<Client> addClient(Client client) {
        return clientMapper.save(client);
    }

    public Mono<Client> editClient(Client client) {
        return clientMapper.save(client);
    }

    /**
     * Deletes a client by id.
     *
     * @param id client id
     * @return completion when done
     */
    public Mono<Void> deleteClient(Long id) {
        return clientMapper.deleteById(id);
    }
}
