package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.CreateBackupRequest;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.repository.BackupMapper;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.NodeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CMBackupServiceTest {
    /*
    @Mock
    private RegistryService registryService;

    @Mock
    private BackupMapper backupMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BackupService backupService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(backupService, "backupManagerBaseUrl", "node2:8082");
    }

    @Test
    void createBackup_withActiveNodes_shouldCreateQueuedBackupAndForwardSavedDto() {
        // Arrange
        List<NodeDTO> nodes = List.of(
                new NodeDTO(1L, "node1:8081", "node1:8081", NodeStatus.ACTIVE, NodeMode.NODE, LocalDateTime.now()),
                new NodeDTO(2L, "node2:8082", "node2:8082", NodeStatus.ACTIVE, NodeMode.NODE, LocalDateTime.now())
        );
        when(registryService.getActiveNodes()).thenReturn(nodes);

        BackupDTO saved = new BackupDTO(
                42L,
                1L,
                1L,
                "Backup-1",
                BackupState.QUEUED,
                100L,
                null,
                null,
                LocalDateTime.now(),
                List.of("node1:8081", "node2:8082")
        );

        BackupService spyService = spy(backupService);
        doReturn(saved).when(spyService).store(any());

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act
        BackupDTO returned = spyService.createBackup(request);

        // Assert
        assertThat(returned.getId()).isNull();
        assertThat(returned.getClientId()).isEqualTo(1L);
        assertThat(returned.getTaskId()).isEqualTo(1L);
        assertThat(returned.getSizeBytes()).isEqualTo(100L);
        assertThat(returned.getState()).isEqualTo(BackupState.QUEUED);
        assertThat(returned.getReplicationNodes())
                .containsExactlyInAnyOrder("node1:8081", "node2:8082");

        ArgumentCaptor<BackupDTO> toStoreCaptor = ArgumentCaptor.forClass(BackupDTO.class);
        verify(spyService, times(1)).store(toStoreCaptor.capture());
        BackupDTO toStore = toStoreCaptor.getValue();
        assertThat(toStore.getId()).isNull();
        assertThat(toStore.getState()).isEqualTo(BackupState.QUEUED);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BackupDTO> forwardedCaptor = ArgumentCaptor.forClass(BackupDTO.class);
        verify(restTemplate, times(1))
                .postForEntity(urlCaptor.capture(), forwardedCaptor.capture(), eq(Void.class));

        assertThat(urlCaptor.getValue())
                .isEqualTo("http://node2:8082/api/v1/bm/backups");

        BackupDTO forwarded = forwardedCaptor.getValue();
        assertThat(forwarded.getId()).isEqualTo(42L);
        assertThat(forwarded.getClientId()).isEqualTo(1L);
        assertThat(forwarded.getTaskId()).isEqualTo(1L);
        assertThat(forwarded.getState()).isEqualTo(BackupState.QUEUED);
    }

    @Test
    void createBackup_withNoActiveNodes_shouldThrowAndNotCallStoreNorBm() {
        // Arrange
        when(registryService.getActiveNodes()).thenReturn(List.of());
        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act & Assert
        assertThatThrownBy(() -> backupService.createBackup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No active nodes available");

        verifyNoInteractions(backupMapper);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void createBackup_whenForwardingFails_shouldStillHaveStoredBackupAndThrow() {
        // Arrange
        List<NodeDTO> nodes = List.of(
                new NodeDTO(1L, "node2:8082", "node2:8082", NodeStatus.ACTIVE, NodeMode.NODE, LocalDateTime.now())
        );
        when(registryService.getActiveNodes()).thenReturn(nodes);

        BackupDTO saved = new BackupDTO(
                77L, 1L, 1L, "Backup-1",
                BackupState.QUEUED, 100L,
                null, null, LocalDateTime.now(),
                List.of("node2:8082")
        );

        BackupService spyService = spy(backupService);
        doReturn(saved).when(spyService).store(any());

        doThrow(new RuntimeException("Connection refused"))
                .when(restTemplate).postForEntity(anyString(), any(), eq(Void.class));

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act & Assert
        assertThatThrownBy(() -> spyService.createBackup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Connection refused");

        verify(spyService, times(1)).store(any());
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(Void.class));
    }

     */
}
