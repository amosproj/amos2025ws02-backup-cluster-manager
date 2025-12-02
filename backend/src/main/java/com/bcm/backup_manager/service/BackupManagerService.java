package com.bcm.backup_manager.service;
import com.bcm.shared.model.api.ExecuteBackupRequest;
import com.bcm.shared.model.api.BackupDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    public boolean execute(Long id, ExecuteBackupRequest request) {

        List<String> nodes = request.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            return false;
        }

        List<CompletableFuture<Boolean>> futures = nodes.stream().map(nodeAddress -> CompletableFuture.supplyAsync(() -> {
                    try {
                        String url = "http://" + nodeAddress + backupNodeApiPath + "/backups/" + id + "/execute";

                        ResponseEntity<Void> response = restTemplate.postForEntity(
                                url,
                                request,
                                Void.class
                        );

                        return response.getStatusCode().is2xxSuccessful();
                    } catch (Exception e) {
                        return false;
                    }
                })).toList();

        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        all.join();

        boolean allSucceeded = futures.stream().allMatch(f -> {
                    try {
                        return f.get();
                    } catch (InterruptedException | ExecutionException e) {
                        return false;
                    }
                });

        return allSucceeded && request.getShouldSucceed();
    }
}