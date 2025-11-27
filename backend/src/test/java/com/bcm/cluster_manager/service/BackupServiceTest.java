package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.CreateBackupRequest;
import com.bcm.cluster_manager.repository.BackupMapper;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeStatus;
import com.bcm.cluster_manager.model.database.BackupState;
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
class BackupServiceTest {
    /*
    @Mock private RegistryService registryService;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private BackupService backupService;

    @Mock private BackupMapper backupMapper;

    @BeforeEach
    void wirePrivateFields() {
        ReflectionTestUtils.setField(backupService, "registryService", registryService);
        ReflectionTestUtils.setField(backupService, "backupManagerBaseUrl", "node2:8082");
        ReflectionTestUtils.setField(backupService, "backupMapper", backupMapper);
    }

    @Test
    void createBackup_withActiveNodes_shouldCreateBackupDTO_andStoreInCM_andForwardSavedDTO() {
        // Arrange
        Collection<NodeDTO> nodes = Arrays.asList(
                new NodeDTO(1L, "node1:8081", "node1:8081", NodeStatus.ACTIVE, LocalDateTime.now()),
                new NodeDTO(2L, "node2:8082", "node2:8082", NodeStatus.ACTIVE, LocalDateTime.now())
        );
        when(registryService.getActiveNodes()).thenReturn(nodes);
        when(backupMapper.insert(any())).thenReturn(1);
        // The storage returns the saved DTO (with id) that will be forwarded to BM
        BackupDTO saved = new BackupDTO(
                42L, 1L, 1L, "Backup-1",
                BackupState.RUNNING, 100L,
                null, null, LocalDateTime.now(),
                List.of("node1:8081", "node2:8082")
        );
        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act
        BackupDTO returned = backupService.createBackup(request);

        // Assert: stored once
        verify(backupMapper, times(1)).insert(any());
        // Assert: forwarded the SAVED DTO to BM
        ArgumentCaptor<String> urlCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BackupDTO> forwardedCap = ArgumentCaptor.forClass(BackupDTO.class);
        verify(restTemplate, times(1)).postForEntity(urlCap.capture(), forwardedCap.capture(), eq(Void.class));
        assertThat(urlCap.getValue()).isEqualTo("http://node2:8082/api/v1/bm/backups");

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
        when(backupService.store(any())).thenReturn(
                new BackupDTO(10L, 1L, 1L, "Backup-1", BackupState.RUNNING, 100L, null, null, LocalDateTime.now(),
                        List.of("node1:8081", "node2:8082", "node3:8083"))
        );

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act
        BackupDTO returned = backupService.createBackup(request);

        // Assert
        assertThat(returned.getReplicationNodes())
                .containsExactlyInAnyOrder("node1:8081", "node2:8082", "node3:8083");
        verify(backupService).store(any());
        verify(restTemplate).postForEntity(anyString(), any(BackupDTO.class), eq(Void.class));
    }

    @Test
    void createBackup_withNoActiveNodes_shouldThrowException_andNotCallStorageNorBM() {
        // Arrange
        when(registryService.getActiveNodes()).thenReturn(Collections.emptyList());
        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act & Assert
        assertThatThrownBy(() -> backupService.createBackup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No active nodes available");

        verifyNoInteractions(backupService, restTemplate);
    }

    @Test
    void createBackup_whenBackupManagerUnreachable_shouldThrow_andStillStoreInCM() {
        // Arrange
        when(registryService.getActiveNodes()).thenReturn(
                List.of(new NodeDTO(1L, "node2:8082", "node2:8082", NodeStatus.ACTIVE, LocalDateTime.now()))
        );
        when(backupService.store(any())).thenReturn(
                new BackupDTO(77L, 1L, 1L, "Backup-1", BackupState.RUNNING, 100L, null, null, LocalDateTime.now(),
                        List.of("node2:8082"))
        );
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenThrow(new RestClientException("Connection refused"));

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act & Assert
        assertThatThrownBy(() -> backupService.createBackup(request))
                .isInstanceOf(RestClientException.class);

        verify(backupService, times(1)).store(any());
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
        when(backupService.store(any())).thenReturn(saved);

        CreateBackupRequest request = new CreateBackupRequest(5L, 15L, 512L);

        // Act
        backupService.createBackup(request);

        // Assert
        ArgumentCaptor<BackupDTO> dtoCaptor = ArgumentCaptor.forClass(BackupDTO.class);
        verify(restTemplate).postForEntity(anyString(), dtoCaptor.capture(), eq(Void.class));
        BackupDTO sent = dtoCaptor.getValue();
        assertThat(sent.getId()).isEqualTo(10L);
        assertThat(sent.getClientId()).isEqualTo(5L);
        assertThat(sent.getTaskId()).isEqualTo(15L);
        assertThat(sent.getSizeBytes()).isEqualTo(512L);
        assertThat(sent.getState()).isEqualTo(BackupState.RUNNING);
    } */
}
