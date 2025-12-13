package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.BigClientDTO;
import com.bcm.shared.model.api.ClientDTO;
import com.bcm.shared.model.api.NodeDTO;
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
import org.w3c.dom.Node;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class CMClientService {

    @Autowired
    private RegistryService registryService;

    @Autowired
    private RestTemplate restTemplate;

    public List<BigClientDTO> getAllClients() {
        Collection<NodeDTO> nodeAddresses = registryService.getActiveNodes();
        if (nodeAddresses.isEmpty()) return List.of();

        List<CompletableFuture<BigClientDTO[]>> futures = nodeAddresses.stream().map(node -> CompletableFuture.supplyAsync(() -> {
            try {
                String url = "http://" + node.getAddress() + "/api/v1/bn/clients";
                ResponseEntity<ClientDTO[]> response = restTemplate.getForEntity(url, ClientDTO[].class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return Arrays.stream(response.getBody()).map(clientDto -> {;
                        BigClientDTO bigClientDto = new BigClientDTO();
                        bigClientDto.setId(clientDto.getId());
                        bigClientDto.setNameOrIp(clientDto.getNameOrIp());
                        bigClientDto.setEnabled(clientDto.isEnabled());
                        bigClientDto.setNodeDTO(node);
                        return bigClientDto;
                    }).toArray(BigClientDTO[]::new);
                }
            } catch (Exception e) {
                System.out.println("Fehler beim Abruf von Tasks von Node " + node.getAddress());
            }
            return null;
        })).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<BigClientDTO> allClients = new ArrayList<>();
        //Set<Long> seenIds = new HashSet<>();

        for (CompletableFuture<BigClientDTO[]> future : futures) {
            try {
                BigClientDTO[] clients = future.get();
                if (clients != null) {
                    for (BigClientDTO client : clients) {
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
