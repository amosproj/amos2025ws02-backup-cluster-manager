package com.bcm.backup_manager.controller;

import com.bcm.backup_manager.service.BackupManagerService;
import com.bcm.backup_node.service.BackupNodeService;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.cluster_manager.model.database.BackupState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupManagerServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private BackupNodeService backupNodeService;

    @InjectMocks
    private BackupManagerService backupManagerService;

    @Test
    void distributeBackup_shouldSendToAllNodes_andStoreLocally() {
        // Arrange
        BackupDTO dto = new BackupDTO(
                null, 1L, 1L, "Backup-1",
                BackupState.QUEUED, 100L,
                LocalDateTime.now(), null, LocalDateTime.now(),
                Arrays.asList("node1:8081", "node2:8082")
        );

        // Act
        backupManagerService.distributeBackup(dto);

        // Assert
        verify(restTemplate, times(2)).postForEntity(anyString(), eq(dto), eq(Void.class));
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    void distributeBackup_whenNodeUnavailable_shouldContinue() {
        // Arrange
        BackupDTO dto = new BackupDTO(
                null, 1L, 1L, "Backup-1",
                BackupState.QUEUED, 100L,
                LocalDateTime.now(), null, LocalDateTime.now(),
                Arrays.asList("node1:8081", "node2:8082")
        );

        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenThrow(new RestClientException("Connection refused"));

        // Act & Assert - should not throw exception
        assertThatCode(() -> backupManagerService.distributeBackup(dto))
                .doesNotThrowAnyException();

        verify(backupNodeService, times(0)).storeBackup(dto);
        verify(restTemplate, times(2)).postForEntity(anyString(), eq(dto), eq(Void.class));
        verifyNoMoreInteractions(restTemplate);
        verifyNoInteractions(backupNodeService);
    }

    @Test
    void distributeBackup_shouldBuildCorrectUrls_andCallEachOnce() {
        // Arrange
        BackupDTO dto = new BackupDTO(
                null, 5L, 9L, "Backup-9",
                BackupState.QUEUED, 10_000L,
                LocalDateTime.now(), null, LocalDateTime.now(),
                Arrays.asList("alpha:7001", "beta:7002", "gamma:7003")
        );

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        backupManagerService.distributeBackup(dto);

        // Assert
        verify(backupNodeService, times(0)).storeBackup(dto);
        verify(restTemplate, times(3)).postForEntity(urlCaptor.capture(), eq(dto), eq(Void.class));

        List<String> urls = urlCaptor.getAllValues();
        assertThat(urls).containsExactlyInAnyOrder(
                "http://alpha:7001/api/v1/bn/backups/sync",
                "http://beta:7002/api/v1/bn/backups/sync",
                "http://gamma:7003/api/v1/bn/backups/sync"
        );
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    void distributeBackup_manyNodes_mixedFailures_shouldAttemptAllAndNotThrow() {
        // Arrange
        List<String> nodes = Arrays.asList(
                "n1:8001", "n2:8002", "n3:8003", "n4:8004", "n5:8005"
        );
        BackupDTO dto = new BackupDTO(
                null, 2L, 3L, "Backup-3",
                BackupState.QUEUED, 2048L,
                LocalDateTime.now(), null, LocalDateTime.now(),
                nodes
        );

        // Fail for n2 and n4, succeed for others
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0, String.class);
                    if (url.contains("n2:8002") || url.contains("n4:8004")) {
                        throw new RestClientException("Simulated failure for " + url);
                    }
                    return null;
                });

        // Act & Assert
        assertThatCode(() -> backupManagerService.distributeBackup(dto))
                .doesNotThrowAnyException();

        // Assert: all nodes were attempted
        verify(backupNodeService, times(0)).storeBackup(dto);
        verify(restTemplate, times(nodes.size())).postForEntity(anyString(), eq(dto), eq(Void.class));

        // Optionally capture to ensure all expected URLs were hit
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate, times(nodes.size())).postForEntity(urlCaptor.capture(), eq(dto), eq(Void.class));
        assertThat(urlCaptor.getAllValues()).containsExactlyInAnyOrder(
                "http://n1:8001/api/v1/bn/backups/sync",
                "http://n2:8002/api/v1/bn/backups/sync",
                "http://n3:8003/api/v1/bn/backups/sync",
                "http://n4:8004/api/v1/bn/backups/sync",
                "http://n5:8005/api/v1/bn/backups/sync"
        );

        verifyNoMoreInteractions(restTemplate);
        verifyNoInteractions(backupNodeService);
    }

}