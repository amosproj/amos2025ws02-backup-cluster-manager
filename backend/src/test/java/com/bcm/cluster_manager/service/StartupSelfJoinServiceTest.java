package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.RegisterRequest;
import com.bcm.shared.service.StartupSelfJoinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled("Skipping Spring context startup for now")
class StartupSelfJoinServiceTest {
    /*
    private StartupSelfJoinService startupService;

    @BeforeEach
    void setUp() {
        startupService = new StartupSelfJoinService(new WebClient.Builder());

        ReflectionTestUtils.setField(startupService, "nodePublicAddress", "localhost:8080");
        ReflectionTestUtils.setField(startupService, "cmPublicAddress", "localhost:8080");
    }

    @Test
    void registerSelf_shouldCallService_withClusterManagerMode() {

        startupService.run(null);

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(webClient, times(1)).post(captor.capture());

        RegisterRequest capturedRequest = captor.getValue();
        assertEquals("localhost:8080", capturedRequest.getAddress());
    }

    @Test
    void registerSelf_shouldDoNothing_ifAlreadyRegistered() {

        ReflectionTestUtils.setField(startupService, "hasJoined", true);

        startupService.run(null);

        verify(nodeManagementService, never()).registerNode(any());
    }


     */
}