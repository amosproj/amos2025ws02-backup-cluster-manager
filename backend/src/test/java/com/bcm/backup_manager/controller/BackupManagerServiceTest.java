package com.bcm.backup_manager;

import com.bcm.backup_node.BackupNodeService;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.database.BackupState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("backup_manager")
class BackupManagerServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private BackupNodeService backupNodeService;

    @InjectMocks
    private BackupManagerService backupManagerService;

    @Test
    void distributeBackup_shouldSendToAllNodes() {
        // Arrange
        ReflectionTestUtils.setField(backupManagerService, "serverPort", 9999);

        BackupDTO dto = new BackupDTO(
                null, 1L, 1L, "Backup-1",
                BackupState.QUEUED, 100L,
                LocalDateTime.now(), null, LocalDateTime.now(),
                Arrays.asList("node1:8081", "node2:8082")
        );

        // Act
        backupManagerService.distributeBackup(dto);

        // Assert
        verify(restTemplate, times(2)).postForEntity(
                anyString(),
                eq(dto),
                eq(Void.class)
        );
        verify(backupNodeService, times(1)).storeBackup(dto);
    }

    @Test
    void distributeBackup_shouldSkipSelfNode() {
        // Arrange
        ReflectionTestUtils.setField(backupManagerService, "serverPort", 8082);

        BackupDTO dto = new BackupDTO(
                null, 1L, 1L, "Backup-1",
                BackupState.QUEUED, 100L,
                LocalDateTime.now(), null, LocalDateTime.now(),
                Arrays.asList("node1:8081", "node2:8082")
        );

        // Act
        backupManagerService.distributeBackup(dto);

        // Assert
        // Should only send to node1, skip node2 (self)
        verify(restTemplate, times(1)).postForEntity(
                contains("node1:8081"),
                eq(dto),
                eq(Void.class)
        );
        verify(restTemplate, never()).postForEntity(
                contains("8082"),
                any(),
                any()
        );
        verify(backupNodeService, times(1)).storeBackup(dto);
    }

    @Test
    void distributeBackup_whenNodeUnavailable_shouldContinue() {
        // Arrange
        ReflectionTestUtils.setField(backupManagerService, "serverPort", 9999);

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

        verify(backupNodeService, times(1)).storeBackup(dto);
    }

    @Test
    void distributeBackup_withoutBackupNodeService_shouldOnlyDistribute() {
        // Arrange
        ReflectionTestUtils.setField(backupManagerService, "backupNodeService", null);
        ReflectionTestUtils.setField(backupManagerService, "serverPort", 9999);

        BackupDTO dto = new BackupDTO(
                null, 1L, 1L, "Backup-1",
                BackupState.QUEUED, 100L,
                LocalDateTime.now(), null, LocalDateTime.now(),
                Arrays.asList("node1:8081")
        );

        // Act
        backupManagerService.distributeBackup(dto);

        // Assert
        verify(restTemplate, times(1)).postForEntity(
                anyString(),
                eq(dto),
                eq(Void.class)
        );
    }

    @Test
    void distributeBackup_withEmptyNodeList_shouldOnlyStoreLocally() {
        // Arrange
        BackupDTO dto = new BackupDTO(
                null, 1L, 1L, "Backup-1",
                BackupState.RUNNING, 100L,
                LocalDateTime.now(), null, LocalDateTime.now(),
                Collections.emptyList()
        );

        // Act
        backupManagerService.distributeBackup(dto);

        // Assert
        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
        verify(backupNodeService, times(1)).storeBackup(dto);
    }

    @Test
    void distributeBackup_withNullNodeList_shouldOnlyStoreLocally() {
        // Arrange
        BackupDTO dto = new BackupDTO(
                null, 1L, 1L, "Backup-1",
                BackupState.QUEUED, 100L,
                LocalDateTime.now(), null, LocalDateTime.now(),
                null
        );

        // Act
        backupManagerService.distributeBackup(dto);

        // Assert
        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
        verify(backupNodeService, times(1)).storeBackup(dto);
    }

    @Test
    void distributeBackup_localStorageFails_shouldContinueDistributing() {
        // Arrange
        ReflectionTestUtils.setField(backupManagerService, "serverPort", 9999);

        BackupDTO dto = new BackupDTO(
                null, 1L, 1L, "Backup-1",
                BackupState.COMPLETED, 100L,
                LocalDateTime.now(), null, LocalDateTime.now(),
                Arrays.asList("node1:8081")
        );

        doThrow(new RuntimeException("Database error"))
                .when(backupNodeService).storeBackup(any());

        // Act & Assert - should not throw
        assertThatCode(() -> backupManagerService.distributeBackup(dto))
                .doesNotThrowAnyException();

        verify(restTemplate, times(1)).postForEntity(anyString(), any(), any());
    }
}