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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NodeStartupRegisterTests {

    private NodeStartupRegister nodeStartupRegister;
    private RestTemplate restTemplateMock;
        private Environment environment;


    @BeforeEach
    void setup() {
        environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        nodeStartupRegister = new NodeStartupRegister(environment);

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
        doReturn(null).when(restTemplateMock).postForEntity(anyString(), any(), any());

        ApplicationArguments args = mock(ApplicationArguments.class);

        nodeStartupRegister.registerAtStartup().run(args);

        ArgumentCaptor<RegisterRequest> requestCaptor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(restTemplateMock, times(1))
            .postForEntity(eq("http://localhost:8080/api/v1/cm/register"), requestCaptor.capture(), eq((Class)Void.class));
        assertThat(requestCaptor.getValue().getAddress()).isEqualTo("localhost:8081");
        assertThat(requestCaptor.getValue().getMode()).isEqualTo(NodeMode.NODE);
    }

    @Test
    void registerAtStartup_retriesOnFailure() throws Exception {
        doThrow(new RuntimeException("Connection failed")).when(restTemplateMock).postForEntity(anyString(), any(), any());

        ApplicationArguments args = mock(ApplicationArguments.class);

        try {
            nodeStartupRegister.registerAtStartup().run(args);
        } catch (InterruptedException ignored) {
        }

    verify(restTemplateMock, times(10))
        .postForEntity(anyString(), any(RegisterRequest.class), eq((Class)Void.class));
    }
}