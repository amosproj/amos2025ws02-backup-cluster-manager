package com.bcm.backup_manager;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.service.BackupDataStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class BackupManagerService {

    private final BackupDataStorageService backupDataStorageService;
    private final RestTemplate restTemplate;

    @Value("${server.port}")
    private int serverPort;

    public BackupManagerService(BackupDataStorageService backupDataStorageService, RestTemplate restTemplate) {
        this.backupDataStorageService = backupDataStorageService;
        this.restTemplate = restTemplate;
    }

    public void distributeBackup(BackupDTO dto) {

        try {
            backupDataStorageService.storeBackupData(dto);
        } catch (Exception e) {
            System.out.println("Failed to store locally (bm)");
        }

        List<String> nodes = dto.getReplicationNodes();
        if (nodes == null) return;

        for (String nodeAddress : nodes) {

            if (nodeAddress.contains(":" + serverPort)) {
                continue;
            }

            try {
                String url = "http://" + nodeAddress + "/api/v1/backups/sync";
                restTemplate.postForEntity(url, dto, Void.class);
            } catch (Exception e) {
                System.out.println("Failed to distribute to " + nodeAddress);
            }
        }

    }
}