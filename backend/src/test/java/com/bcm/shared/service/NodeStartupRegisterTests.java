package com.bcm.shared.service;

import com.bcm.shared.model.api.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NodeStartupRegisterTests {

    private NodeStartupRegister nodeStartupRegister;
    private RestTemplate restTemplateMock;

    @BeforeEach
    void setup() {
        nodeStartupRegister = new NodeStartupRegister(null);

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
        when(restTemplateMock.postForEntity(anyString(), any(RegisterRequest.class), eq(Void.class)))
                .thenReturn(null);

        ApplicationArguments args = mock(ApplicationArguments.class);

        nodeStartupRegister.registerAtStartup().run(args);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RegisterRequest> requestCaptor = ArgumentCaptor.forClass(RegisterRequest.class);

        verify(restTemplateMock, times(1))
                .postForEntity(urlCaptor.capture(), requestCaptor.capture(), eq(Void.class));

        assertThat(urlCaptor.getValue()).isEqualTo("http://localhost:8080/api/v1/cm/register");
        assertThat(requestCaptor.getValue().getAddress()).isEqualTo("localhost:8081");
    }

    @Test
    void registerAtStartup_retriesOnFailure() throws Exception {
        when(restTemplateMock.postForEntity(anyString(), any(RegisterRequest.class), eq(Void.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        ApplicationArguments args = mock(ApplicationArguments.class);

        try {
            nodeStartupRegister.registerAtStartup().run(args);
        } catch (InterruptedException ignored) {
        }

        verify(restTemplateMock, times(10))
                .postForEntity(anyString(), any(RegisterRequest.class), eq(Void.class));
    }
}