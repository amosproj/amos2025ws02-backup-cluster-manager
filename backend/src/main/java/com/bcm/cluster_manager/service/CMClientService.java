package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.ClientDTO;
import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.model.database.Client;
import com.bcm.shared.repository.ClientMapper;
import com.bcm.shared.util.NodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class CMClientService {

    @Autowired
    private RegistryService registryService;

    @Autowired
    private RestTemplate restTemplate;

    public List<ClientDTO> getAllClients() {
        List<String> nodeAddresses = NodeUtils.addresses(registryService.getActiveNodes());
        if (nodeAddresses.isEmpty()) return List.of();

        List<CompletableFuture<ClientDTO[]>> futures = nodeAddresses.stream().map(address -> CompletableFuture.supplyAsync(() -> {
            try {
                String url = "http://" + address + "/api/v1/bn/clients";
                ResponseEntity<ClientDTO[]> response = restTemplate.getForEntity(url, ClientDTO[].class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } catch (Exception e) {
                System.out.println("Fehler beim Abruf von Tasks von Node " + address);
            }
            return null;
        })).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<ClientDTO> allClients = new ArrayList<>();
        //Set<Long> seenIds = new HashSet<>();

        for (CompletableFuture<ClientDTO[]> future : futures) {
            try {
                ClientDTO[] clients = future.get();
                if (clients != null) {
                    for (ClientDTO client : clients) {
                        //if (client != null && seenIds.add(client.getId())) {
                            allClients.add(client);
                       // }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return allClients;
    }
}
