package com.bcm.backup_node.service;

import com.bcm.backup_node.model.database.BackupData;
import com.bcm.backup_node.repository.BackupDataMapper;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.ExecuteBackupRequest;
import com.bcm.shared.service.BackupNodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class BackupNodeServiceTests {

    private BackupDataMapper backupDataMapper;
    private BackupNodeService backupNodeService;

    @BeforeEach
    void setUp() {
        backupDataMapper = mock(BackupDataMapper.class);
        backupNodeService = new BackupNodeService(backupDataMapper);
    }

    @Test
    void storeBackup_shouldInsertBackupDataWithGivenId() {
        // Arrange
        BackupDTO dto = new BackupDTO(
                10L,           // id
                1L,            // clientId
                1L,            // taskId
                "name",
                null,          // state
                123L,          // sizeBytes
                null,
                null,
                LocalDateTime.now(), // createdAt
                List.of()      // replicationNodes
        );

        // Act
        backupNodeService.storeBackup(dto);

        // Assert
        ArgumentCaptor<BackupData> captor = ArgumentCaptor.forClass(BackupData.class);
        verify(backupDataMapper, times(1)).insert(captor.capture());

        BackupData stored = captor.getValue();
        assertThat(stored.getId()).isEqualTo(10L);
        assertThat(stored.getBackup_data()).isEqualTo("{\"mock\": \"backup content\"}");
        assertThat(stored.getCreatedAt()).isNotNull();
    }

    @Test
    void deleteBackupData_shouldCallMapperDelete() {
        // Arrange
        Long backupId = 5L;

        // Act
        backupNodeService.deleteBackupData(backupId);

        // Assert
        verify(backupDataMapper, times(1)).delete(backupId);
        verifyNoMoreInteractions(backupDataMapper);
    }

    @Test
    void executeBackupSync_shouldFindAndUpdateBackupData() {
        // Arrange
        Long id = 7L;
        ExecuteBackupRequest request = new ExecuteBackupRequest(id ,true, null); // 50 ms

        BackupData existing = new BackupData();
        existing.setId(id);
        existing.setBackup_data("{\"mock\": \"backup content\"}");
        existing.setCreatedAt(Instant.now());

        when(backupDataMapper.findById(anyLong())).thenReturn(existing);

        // Act
        backupNodeService.executeBackupSync(id, request);

        // Assert
        verify(backupDataMapper, times(1)).findById(id);

        ArgumentCaptor<BackupData> captor = ArgumentCaptor.forClass(BackupData.class);
        verify(backupDataMapper, times(1)).update(captor.capture());

        BackupData updated = captor.getValue();
        assertThat(updated.getId()).isEqualTo(id);
        assertThat(updated.getBackup_data())
                .startsWith("{\"mock\": \"backup content updated ");
    }
}
