package com.bcm.backup_manager;
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

    public void distributeBackup(BackupDTO dto) {
        List<String> nodes = dto.getReplicationNodes();
        if (nodes == null) return;

        for (String nodeAddress : nodes) {
            try {
                String url = "http://" + nodeAddress + "/api/v1/backups/sync";
                restTemplate.postForEntity(url, dto, Void.class);
            } catch (Exception e) {
                System.out.println("Failed to distribute to " + nodeAddress);
            }
        }

    }
}