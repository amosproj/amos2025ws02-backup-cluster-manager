package com.bcm.cluster_manager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

import com.bcm.shared.model.api.NodeMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ClusterManagerSelfRegisterTest {

    private ClusterManagerSelfRegister clusterManagerSelfRegister;
    private RegistryService registryServiceMock;

    @BeforeEach
    void setup() {
        clusterManagerSelfRegister = new ClusterManagerSelfRegister();
        registryServiceMock = mock(RegistryService.class);

        try {
            var registryField = ClusterManagerSelfRegister.class.getDeclaredField("registryService");
            registryField.setAccessible(true);
            registryField.set(clusterManagerSelfRegister, registryServiceMock);

            var addressField = ClusterManagerSelfRegister.class.getDeclaredField("nodePublicAddress");
            addressField.setAccessible(true);
            addressField.set(clusterManagerSelfRegister, "cluster-manager:8080");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testRegisterClusterManagerAsNode() throws Exception {
        // Given
        ApplicationArguments args = mock(ApplicationArguments.class);

        // When
        clusterManagerSelfRegister.registerClusterManagerAsNode().run(args);

        // Then
        verify(registryServiceMock, times(1)).register("cluster-manager:8080", NodeMode.CLUSTER_MANAGER);
    }

    @Test
    void testRegisterClusterManagerAsNodeWithException() throws Exception {
        // Given
        ApplicationArguments args = mock(ApplicationArguments.class);
        doThrow(new RuntimeException("Test exception")).when(registryServiceMock).register("cluster-manager:8080", NodeMode.CLUSTER_MANAGER);

        // When - Should not throw exception, just log the error
        clusterManagerSelfRegister.registerClusterManagerAsNode().run(args);

        // Then
        verify(registryServiceMock, times(1)).register("cluster-manager:8080", NodeMode.CLUSTER_MANAGER);
    }

    @Test
    void testApplicationRunnerBeanCreation() {
        // When
        var runner = clusterManagerSelfRegister.registerClusterManagerAsNode();

        // Then
        assertThat(runner).isNotNull();
    }
}
