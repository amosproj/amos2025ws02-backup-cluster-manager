package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.model.api.BigTaskDTO;
import com.bcm.cluster_manager.service.CMTaskService;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CMTaskControllerTest {

    @Mock
    private CMTaskService cmTaskService;

    private CMTaskController controller;

    @BeforeEach
    void setUp() {
        controller = new CMTaskController();

        try {
            var f = CMTaskController.class.getDeclaredField("CMTaskService");
            f.setAccessible(true);
            f.set(controller, cmTaskService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getTasks_delegatesToService() {
        PaginationRequest pagination = new PaginationRequest();

        PaginationResponse<BigTaskDTO> expected =
                new PaginationResponse<>(List.of(new BigTaskDTO()), 1L, 1L, 1L);

        when(cmTaskService.getPaginatedItems(any(PaginationRequest.class)))
                .thenReturn(Mono.just(expected));

        StepVerifier.create(controller.getTasks(pagination))
                .expectNext(expected)
                .verifyComplete();

        verify(cmTaskService).getPaginatedItems(any(PaginationRequest.class));
    }
}
