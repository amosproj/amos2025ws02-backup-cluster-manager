package com.bcm.cluster_manager.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.bcm.cluster_manager.controller.ClusterManagerController;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.repository.BackupMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.bcm.shared.model.api.BackupDTO;
import org.springframework.web.client.RestTemplate;

@Profile("cluster_manager")
@Service
public class BackupService {
    private final RegistryService registryService;
    private final RestTemplate restTemplate;
    private final BackupMapper backupMapper;

    @Value("${backup.manager.url:http://node2:8082}")
    private String backupManagerBaseUrl;

    public BackupService(RegistryService registryService,
                         BackupMapper backupMapper,
                         RestTemplate restTemplate) {
        this.registryService = registryService;
        this.backupMapper = backupMapper;
        this.restTemplate = restTemplate;
    }

    public BackupDTO createBackup(ClusterManagerController.CreateBackupRequest request) {
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
                    backupManagerBaseUrl + "/api/backup-manager/backups",
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

                System.out.println("Fetching backups from: " + url);

                BackupDTO[] nodeBackups = restTemplate.getForObject(url, BackupDTO[].class);

                if (nodeBackups != null && nodeBackups.length > 0) {
                    allBackups.addAll(Arrays.asList(nodeBackups));
                    System.out.println("Got " + nodeBackups.length + " backups from " + nodeAddress);
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
}