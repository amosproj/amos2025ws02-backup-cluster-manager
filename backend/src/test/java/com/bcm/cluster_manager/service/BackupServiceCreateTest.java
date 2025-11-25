package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.dto.CreateBackupRequest;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeStatus;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.service.BackupStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupServiceCreateTest {

    @Mock private RegistryService registryService;
    @Mock private RestTemplate restTemplate;
    @Mock private BackupStorageService backupStorageService;

    @InjectMocks
    private ClusterManagerService clusterManagerService;

    @BeforeEach
    void wirePrivateFields() {
        ReflectionTestUtils.setField(clusterManagerService, "registry", registryService);
        ReflectionTestUtils.setField(clusterManagerService, "registryService", registryService);
        ReflectionTestUtils.setField(clusterManagerService, "backupStorageService", backupStorageService);
        ReflectionTestUtils.setField(clusterManagerService, "backupManagerBaseUrl", "node2:8082");
    }

    @Test
    void createBackup_withActiveNodes_shouldCreateBackupDTO_andStoreInCM_andForwardSavedDTO() {
        // Arrange
        Collection<NodeDTO> nodes = Arrays.asList(
                new NodeDTO(1L, "node1:8081", "node1:8081", NodeStatus.ACTIVE, LocalDateTime.now()),
                new NodeDTO(2L, "node2:8082", "node2:8082", NodeStatus.ACTIVE, LocalDateTime.now())
        );
        when(registryService.getActiveNodes()).thenReturn(nodes);

        // The storage returns the saved DTO (with id) that will be forwarded to BM
        BackupDTO saved = new BackupDTO(
                42L, 1L, 1L, "Backup-1",
                BackupState.RUNNING, 100L,
                null, null, LocalDateTime.now(),
                List.of("node1:8081", "node2:8082")
        );
        when(backupStorageService.store(any())).thenReturn(saved);

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act
        BackupDTO returned = clusterManagerService.createBackup(request);

        // Assert: stored once
        ArgumentCaptor<BackupDTO> builtCaptor = ArgumentCaptor.forClass(BackupDTO.class);
        verify(backupStorageService, times(1)).store(builtCaptor.capture());
        BackupDTO built = builtCaptor.getValue();
        assertThat(built.getId()).isNull();
        assertThat(built.getClientId()).isEqualTo(1L);
        assertThat(built.getTaskId()).isEqualTo(1L);
        assertThat(built.getSizeBytes()).isEqualTo(100L);
        assertThat(built.getState()).isEqualTo(BackupState.RUNNING);

        // Assert: forwarded the SAVED DTO to BM
        ArgumentCaptor<String> urlCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BackupDTO> forwardedCap = ArgumentCaptor.forClass(BackupDTO.class);
        verify(restTemplate, times(1)).postForEntity(urlCap.capture(), forwardedCap.capture(), eq(Void.class));
        assertThat(urlCap.getValue()).isEqualTo("http://node2:8082/api/v1/backups");
        BackupDTO forwarded = forwardedCap.getValue();
        assertThat(forwarded.getId()).isEqualTo(42L);

        // Assert: returned value is the *original* built DTO (per current code)
        assertThat(returned.getId()).isNull();
        assertThat(returned.getClientId()).isEqualTo(1L);
        assertThat(returned.getTaskId()).isEqualTo(1L);
        assertThat(returned.getSizeBytes()).isEqualTo(100L);
        assertThat(returned.getState()).isEqualTo(BackupState.RUNNING);
    }

    @Test
    void createBackup_shouldIncludeAllActiveNodesInReplicationList() {
        // Arrange
        Collection<NodeDTO> nodes = Arrays.asList(
                new NodeDTO(1L, "node1:8081", "node1:8081", NodeStatus.ACTIVE, LocalDateTime.now()),
                new NodeDTO(2L, "node2:8082", "node2:8082", NodeStatus.ACTIVE, LocalDateTime.now()),
                new NodeDTO(3L, "node3:8083", "node3:8083", NodeStatus.ACTIVE, LocalDateTime.now())
        );
        when(registryService.getActiveNodes()).thenReturn(nodes);
        when(backupStorageService.store(any())).thenReturn(
                new BackupDTO(10L, 1L, 1L, "Backup-1", BackupState.RUNNING, 100L, null, null, LocalDateTime.now(),
                        List.of("node1:8081", "node2:8082", "node3:8083"))
        );

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act
        BackupDTO returned = clusterManagerService.createBackup(request);

        // Assert
        assertThat(returned.getReplicationNodes())
                .containsExactlyInAnyOrder("node1:8081", "node2:8082", "node3:8083");
        verify(backupStorageService).store(any());
        verify(restTemplate).postForEntity(anyString(), any(BackupDTO.class), eq(Void.class));
    }

    @Test
    void createBackup_withNoActiveNodes_shouldThrowException_andNotCallStorageNorBM() {
        // Arrange
        when(registryService.getActiveNodes()).thenReturn(Collections.emptyList());
        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act & Assert
        assertThatThrownBy(() -> clusterManagerService.createBackup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No active nodes available");

        verifyNoInteractions(backupStorageService, restTemplate);
    }

    @Test
    void createBackup_whenBackupManagerUnreachable_shouldThrow_andStillStoreInCM() {
        // Arrange
        when(registryService.getActiveNodes()).thenReturn(
                List.of(new NodeDTO(1L, "node2:8082", "node2:8082", NodeStatus.ACTIVE, LocalDateTime.now()))
        );
        when(backupStorageService.store(any())).thenReturn(
                new BackupDTO(77L, 1L, 1L, "Backup-1", BackupState.RUNNING, 100L, null, null, LocalDateTime.now(),
                        List.of("node2:8082"))
        );
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenThrow(new RestClientException("Connection refused"));

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act & Assert
        assertThatThrownBy(() -> clusterManagerService.createBackup(request))
                .isInstanceOf(RestClientException.class);

        verify(backupStorageService, times(1)).store(any());
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), any());
    }

    @Test
    void createBackup_shouldForwardTheSavedDTO_exactPayload() {
        // Arrange
        when(registryService.getActiveNodes()).thenReturn(
                List.of(new NodeDTO(1L, "node2:8082", "node2:8082", NodeStatus.ACTIVE, LocalDateTime.now()))
        );

        BackupDTO saved = new BackupDTO(
                10L, 5L, 15L, "Backup-15",
                BackupState.RUNNING, 512L,
                null, null, LocalDateTime.now(),
                List.of("node2:8082")
        );
        when(backupStorageService.store(any())).thenReturn(saved);

        CreateBackupRequest request = new CreateBackupRequest(5L, 15L, 512L);

        // Act
        clusterManagerService.createBackup(request);

        // Assert
        ArgumentCaptor<BackupDTO> dtoCaptor = ArgumentCaptor.forClass(BackupDTO.class);
        verify(restTemplate).postForEntity(anyString(), dtoCaptor.capture(), eq(Void.class));
        BackupDTO sent = dtoCaptor.getValue();
        assertThat(sent.getId()).isEqualTo(10L);
        assertThat(sent.getClientId()).isEqualTo(5L);
        assertThat(sent.getTaskId()).isEqualTo(15L);
        assertThat(sent.getSizeBytes()).isEqualTo(512L);
        assertThat(sent.getState()).isEqualTo(BackupState.RUNNING);
    }
}
