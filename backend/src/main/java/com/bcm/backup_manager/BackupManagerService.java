package com.bcm.backup_manager;

import com.bcm.backup_node.BackupNodeService;
import com.bcm.shared.model.api.BackupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class BackupManagerService {

    @Autowired(required = false)
    private BackupNodeService backupNodeService;

    @Value("${server.port}")
    private int serverPort;

    private RestTemplate restTemplate = new RestTemplate();

    public void distributeBackup(BackupDTO dto) {

        if (backupNodeService != null) {
            try {
                backupNodeService.storeBackup(dto);
            } catch (Exception e) {
                System.out.println("Failed to store locally: " + e.getMessage());
            }
        }

        List<String> nodes = dto.getReplicationNodes();
        if (nodes != null) {
            for (String nodeAddress : nodes) {

                if (nodeAddress.contains(":" + serverPort)) {
                    continue;
                }

                try {
                    String url = "http://" + nodeAddress + "/api/v1/backups/sync";
                    restTemplate.postForEntity(url, dto, Void.class);
                    //System.out.println("Distributed backup to node: " + nodeAddress);
                } catch (Exception e) {
                    System.out.println("Failed to distribute to " + nodeAddress + ": " + e.getMessage());
                }
            }
        }
    }
}