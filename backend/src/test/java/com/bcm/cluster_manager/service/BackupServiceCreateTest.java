package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.dto.CreateBackupRequest;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.database.BackupState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupServiceCreateTest {

    @Mock
    private RegistryService registryService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ClusterManagerService clusterManagerService;

    @Test
    void createBackup_withActiveNodes_shouldCreateBackupDTO() {
        // Arrange
        Collection<NodeDTO> nodes = Arrays.asList(
                new NodeDTO(1L, "node1:8081", "node1:8081", "Active", LocalDateTime.now()),
                new NodeDTO(2L, "node2:8082", "node2:8082", "Active", LocalDateTime.now())
        );
        when(registryService.getActiveNodes()).thenReturn(nodes);

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act
        BackupDTO result = clusterManagerService.createBackup(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo(1L);
        assertThat(result.getTaskId()).isEqualTo(1L);
        assertThat(result.getSizeBytes()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(BackupState.QUEUED);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getStartTime()).isNull();
        assertThat(result.getStopTime()).isNull();
    }

    @Test
    void createBackup_shouldIncludeAllActiveNodesInReplicationList() {
        // Arrange
        Collection<NodeDTO> nodes = Arrays.asList(
                new NodeDTO(1L, "node1:8081", "node1:8081", "Active", LocalDateTime.now()),
                new NodeDTO(2L, "node2:8082", "node2:8082", "Active", LocalDateTime.now()),
                new NodeDTO(3L, "node3:8083", "node3:8083", "Active", LocalDateTime.now())
        );
        when(registryService.getActiveNodes()).thenReturn(nodes);

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act
        BackupDTO result = clusterManagerService.createBackup(request);

        // Assert
        assertThat(result.getReplicationNodes()).hasSize(3);
        assertThat(result.getReplicationNodes())
                .contains("node1:8081", "node2:8082", "node3:8083");
    }

    @Test
    void createBackup_shouldSetCorrectBackupState() {
        // Arrange
        Collection<NodeDTO> nodes = Arrays.asList(
                new NodeDTO(1L, "node1:8081", "node1:8081", "Active", LocalDateTime.now())
        );
        when(registryService.getActiveNodes()).thenReturn(nodes);

        CreateBackupRequest request = new CreateBackupRequest(5L, 10L, 500L);

        // Act
        BackupDTO result = clusterManagerService.createBackup(request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(BackupState.QUEUED);
    }

    @Test
    void createBackup_withNullSizeBytes_shouldHandleGracefully() {
        // Arrange
        Collection<NodeDTO> nodes = Arrays.asList(
                new NodeDTO(1L, "node1:8081", "node1:8081", "Active", LocalDateTime.now())
        );
        when(registryService.getActiveNodes()).thenReturn(nodes);

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, null);

        // Act
        BackupDTO result = clusterManagerService.createBackup(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSizeBytes()).isNull();
    }

    @Test
    void createBackup_shouldForwardToBackupManager() {
        // Arrange
        Collection<NodeDTO> nodes = Arrays.asList(
                new NodeDTO(1L, "node1:8081", "node1:8081", "Active", LocalDateTime.now())
        );
        when(registryService.getActiveNodes()).thenReturn(nodes);

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        ArgumentCaptor<BackupDTO> dtoCaptor = ArgumentCaptor.forClass(BackupDTO.class);

        // Act
        clusterManagerService.createBackup(request);

        // Assert
        verify(restTemplate, times(1)).postForEntity(
                anyString(), // Don't check URL since we can't mock @Value
                dtoCaptor.capture(),
                eq(Void.class)
        );

        BackupDTO sentDto = dtoCaptor.getValue();
        assertThat(sentDto.getClientId()).isEqualTo(1L);
        assertThat(sentDto.getTaskId()).isEqualTo(1L);
        assertThat(sentDto.getSizeBytes()).isEqualTo(100L);
    }

    @Test
    void createBackup_withNoActiveNodes_shouldThrowException() {
        // Arrange
        when(registryService.getActiveNodes()).thenReturn(Collections.emptyList());
        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act & Assert
        assertThatThrownBy(() -> clusterManagerService.createBackup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No active nodes available");

        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    void createBackup_whenBackupManagerUnreachable_shouldThrowException() {
        // Arrange
        Collection<NodeDTO> nodes = Arrays.asList(
                new NodeDTO(1L, "node1:8081", "node1:8081", "Active", LocalDateTime.now())
        );
        when(registryService.getActiveNodes()).thenReturn(nodes);
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenThrow(new RestClientException("Connection refused"));

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act & Assert
        assertThatThrownBy(() -> clusterManagerService.createBackup(request))
                .isInstanceOf(RuntimeException.class);
    }
}