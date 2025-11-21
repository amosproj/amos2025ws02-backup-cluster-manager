package com.bcm.cluster_manager.service;


import com.bcm.cluster_manager.controller.ClusterManagerController;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.repository.BackupMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.bcm.cluster_manager.dto.CreateBackupRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class ClusterManagerService {

    private final RegistryService registryService;
    private final RestTemplate restTemplate;
    private final BackupMapper backupMapper;

    @Value("${application.bm.public-address:localhost:8082}")
    private String backupManagerBaseUrl;

    public ClusterManagerService(RegistryService registryService,
                         BackupMapper backupMapper,
                         RestTemplate restTemplate) {
        this.registryService = registryService;
        this.backupMapper = backupMapper;
        this.restTemplate = restTemplate;
    }

    public BackupDTO createBackup(CreateBackupRequest request) {
        // select nodes
        List<String> activeNodes = registryService.getActiveNodes().stream()
                .map(NodeDTO::getAddress)
                .toList();

        if (activeNodes.isEmpty()) {
            throw new RuntimeException("No active nodes available");
        }

        BackupDTO dto = new BackupDTO(
                null,
                request.getClientId(),
                request.getTaskId(),
                "Backup-" + request.getTaskId(),
                BackupState.QUEUED,
                request.getSizeBytes(),
                null,
                null,
                LocalDateTime.now(),
                activeNodes
        );

        try {
            restTemplate.postForEntity(
                    "http://" + backupManagerBaseUrl + "/api/bm/backups",
                    dto,
                    Void.class
            );
        } catch (Exception e) {
            System.out.println("Failed to forward to backup_manager: " + e.getMessage());
            throw e;
        }

        return dto;
    }

    public List<BackupDTO> getAllBackups() {
        List<BackupDTO> allBackups = new ArrayList<>();

        // Get registered nodes
        Collection<NodeDTO> activeNodes = registryService.getActiveNodes();

        for (NodeDTO node : activeNodes) {
            String nodeAddress = node.getAddress();
            try {
                String url = nodeAddress.startsWith("http") ?
                        nodeAddress : "http://" + nodeAddress;
                url += "/api/v1/backups";

                //System.out.println("Fetching backups from: " + url);

                BackupDTO[] nodeBackups = restTemplate.getForObject(url, BackupDTO[].class);

                if (nodeBackups != null && nodeBackups.length > 0) {
                    allBackups.addAll(Arrays.asList(nodeBackups));
                    //System.out.println("Got " + nodeBackups.length + " backups from " + nodeAddress);
                } else {
                    System.out.println("No backups on " + nodeAddress);
                }

            } catch (Exception e) {
                System.out.println(" Failed to query " + nodeAddress + ": " + e.getMessage());
            }
        }

        System.out.println("Total backups aggregated: " + allBackups.size());
        return allBackups;
    }


    public List<NodeDTO> getAllNodes() {
        // Mock data for nodes
        return Arrays.asList(
                new NodeDTO(1L, "Node A", "10.100.179.80:9300", "Active", LocalDateTime.now().minusDays(1)),
                new NodeDTO(2L, "Node B", "10.100.179.81:2030" , "Inactive", LocalDateTime.now().minusDays(2)),
                new NodeDTO(3L, "Node C", "10.100.179.82:3333", "Active", LocalDateTime.now().minusDays(3))
        );
    }
}
