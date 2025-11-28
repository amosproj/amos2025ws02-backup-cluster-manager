package com.bcm.backup_manager.service;
import com.bcm.shared.model.api.BackupDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class BackupManagerService {

    private final RestTemplate restTemplate;

    public BackupManagerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    String backupNodeApiPath= "/api/v1/bn/";

    public void distributeBackup(BackupDTO dto) {
        List<String> nodes = dto.getReplicationNodes();
        if (nodes == null) return;

        for (String nodeAddress : nodes) {
            try {
                String url = "http://" + nodeAddress + backupNodeApiPath + "backups/sync";
                restTemplate.postForEntity(url, dto, Void.class);
            } catch (Exception e) {
                System.out.println("Failed to distribute to " + nodeAddress);
            }
        }

    }

    public void deleteBackup(Long backupId, List<String> nodeAddresses) {

        // delete on other nodes from the list CM provided
        if (nodeAddresses != null) {
            for (String nodeAddress : nodeAddresses) {
                try {
                    String url = "http://" + nodeAddress + backupNodeApiPath + "backups/" + backupId;
                    restTemplate.delete(url);
                } catch (Exception e) {
                    System.out.println("Failed to delete backup on " + nodeAddress + ": " + e.getMessage());
                }
            }
        }
    }
}