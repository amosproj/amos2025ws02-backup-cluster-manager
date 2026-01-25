package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.model.api.BigBackupDTO;
import com.bcm.cluster_manager.service.CMBackupService;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CMBackupControllerTest {

    @Mock
    private CMBackupService cmBackupService;

    private CMBackupController controller;

    @BeforeEach
    void setUp() {
        controller = new CMBackupController();
        try {
            var f = CMBackupController.class.getDeclaredField("CMBackupService");
            f.setAccessible(true);
            f.set(controller, cmBackupService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteBackup_successAndError() {
        when(cmBackupService.deleteBackup(7L, "node1:8080")).thenReturn(Mono.empty());

        StepVerifier.create(controller.deleteBackup(7L, "node1:8080"))
                .assertNext(resp -> assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode()))
                .verifyComplete();

        when(cmBackupService.deleteBackup(8L, "node1:8080"))
                .thenReturn(Mono.error(new RuntimeException("error")));

        StepVerifier.create(controller.deleteBackup(8L, "node1:8080"))
                .assertNext(resp -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode()))
                .verifyComplete();

        verify(cmBackupService).deleteBackup(7L, "node1:8080");
        verify(cmBackupService).deleteBackup(8L, "node1:8080");
    }

    @Test
    void createBackup_successAndError() {
        BigBackupDTO req = new BigBackupDTO();
        req.setClientId(1L);
        req.setTaskId(2L);

        BigBackupDTO created = new BigBackupDTO();
        created.setId(99L);

        when(cmBackupService.createBackup(any(BigBackupDTO.class))).thenReturn(Mono.just(created));

        StepVerifier.create(controller.createBackup(req))
                .assertNext(resp -> {
                    assertEquals(HttpStatus.CREATED, resp.getStatusCode());
                    assertNotNull(resp.getBody());
                    assertEquals(99L, resp.getBody().getId());
                })
                .verifyComplete();

        when(cmBackupService.createBackup(any(BigBackupDTO.class)))
                .thenReturn(Mono.error(new RuntimeException("error")));

        StepVerifier.create(controller.createBackup(req))
                .assertNext(resp -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
                    assertNull(resp.getBody());
                })
                .verifyComplete();

        verify(cmBackupService, times(2)).createBackup(any(BigBackupDTO.class));
    }

    @Test
    void getBackups_delegatesToService() {
        PaginationRequest pagination = new PaginationRequest();

        PaginationResponse<BigBackupDTO> expected =
                new PaginationResponse<>(List.of(new BigBackupDTO()), 1L, 1L, 1L);

        when(cmBackupService.getPaginatedItems(any(PaginationRequest.class)))
                .thenReturn(Mono.just(expected));

        StepVerifier.create(controller.getBackups(pagination))
                .expectNext(expected)
                .verifyComplete();

        verify(cmBackupService).getPaginatedItems(any(PaginationRequest.class));
    }
}
