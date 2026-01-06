package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CMStartupSelfJoinServiceTest {

    @Mock
    private NodeManagementService nodeManagementService;

    private CMStartupSelfJoinService startupService;

    @BeforeEach
    void setUp() {
        startupService = new CMStartupSelfJoinService(nodeManagementService);

        ReflectionTestUtils.setField(startupService, "nodePublicAddress", "localhost:8080");
        ReflectionTestUtils.setField(startupService, "cmPublicAddress", "localhost:8080");
    }

    @Test
    void registerSelf_shouldCallService_withClusterManagerMode() {

        startupService.joinCluster();

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(nodeManagementService, times(1)).registerNode(captor.capture());

        RegisterRequest capturedRequest = captor.getValue();
        assertEquals("localhost:8080", capturedRequest.getAddress());
        assertEquals(NodeMode.CLUSTER_MANAGER, capturedRequest.getMode());
    }

    @Test
    void registerSelf_shouldDoNothing_ifAlreadyRegistered() {

        ReflectionTestUtils.setField(startupService, "hasJoined", true);

        startupService.joinCluster();

        verify(nodeManagementService, never()).registerNode(any());
    }
}