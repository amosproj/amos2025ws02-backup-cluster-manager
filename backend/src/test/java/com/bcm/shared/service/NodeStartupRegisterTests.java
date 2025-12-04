package com.bcm.shared.service;

import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NodeStartupRegisterTests {

    private NodeStartupRegister nodeStartupRegister;
    private RestTemplate restTemplateMock;
    private Environment environmentMock;

    @BeforeEach
    void setup() {
        environmentMock = mock(Environment.class);
        nodeStartupRegister = new NodeStartupRegister(environmentMock);

        restTemplateMock = mock(RestTemplate.class);
        try {
            var field = NodeStartupRegister.class.getDeclaredField("restTemplate");
            field.setAccessible(true);
            field.set(nodeStartupRegister, restTemplateMock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            var cmField = NodeStartupRegister.class.getDeclaredField("cmPublicAddress");
            cmField.setAccessible(true);
            cmField.set(nodeStartupRegister, "localhost:8080");

            var nodeField = NodeStartupRegister.class.getDeclaredField("nodePublicAddress");
            nodeField.setAccessible(true);
            nodeField.set(nodeStartupRegister, "localhost:8081");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void registerAtStartup_sendsRequestToCM() throws Exception {
        // active profile should default to BACKUP_NODE when none provided
        when(environmentMock.getActiveProfiles()).thenReturn(new String[]{});
    doReturn(null).when(restTemplateMock).postForEntity(anyString(), any(), any());

        ApplicationArguments args = mock(ApplicationArguments.class);

        nodeStartupRegister.registerAtStartup().run(args);

        ArgumentCaptor<RegisterRequest> requestCaptor = ArgumentCaptor.forClass(RegisterRequest.class);
    verify(restTemplateMock, times(1))
        .postForEntity(eq("http://localhost:8080/api/v1/cm/register"), requestCaptor.capture(), eq((Class)Void.class));
        assertThat(requestCaptor.getValue().getAddress()).isEqualTo("localhost:8081");
    assertThat(requestCaptor.getValue().getMode()).isEqualTo(NodeMode.BACKUP_NODE);
    }

    @Test
    void registerAtStartup_retriesOnFailure() throws Exception {
        when(environmentMock.getActiveProfiles()).thenReturn(new String[]{});
    doThrow(new RuntimeException("Connection failed")).when(restTemplateMock).postForEntity(anyString(), any(), any());

        ApplicationArguments args = mock(ApplicationArguments.class);

        try {
            nodeStartupRegister.registerAtStartup().run(args);
        } catch (InterruptedException ignored) {
        }

    verify(restTemplateMock, times(10))
        .postForEntity(anyString(), any(RegisterRequest.class), eq((Class)Void.class));
    }

    @Test
    void registerAtStartup_setsBackupManagerModeWhenProfileActive() throws Exception {
        when(environmentMock.getActiveProfiles()).thenReturn(new String[]{"backup_manager"});
    doReturn(null).when(restTemplateMock).postForEntity(anyString(), any(), any());

        nodeStartupRegister.registerAtStartup().run(mock(ApplicationArguments.class));

        ArgumentCaptor<RegisterRequest> requestCaptor = ArgumentCaptor.forClass(RegisterRequest.class);
    verify(restTemplateMock).postForEntity(anyString(), requestCaptor.capture(), eq((Class)Void.class));
    assertThat(requestCaptor.getValue().getMode()).isEqualTo(NodeMode.BACKUP_MANAGER);
    }
}