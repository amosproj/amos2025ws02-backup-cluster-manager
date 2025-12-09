package com.bcm.shared.service;

import com.bcm.shared.model.api.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NodeStartupRegisterServiceTests {

    private NodeStartupRegisterService nodeStartupRegisterService;
    private RestTemplate restTemplateMock;

    @BeforeEach
    void setup() {
        nodeStartupRegisterService = new NodeStartupRegisterService();

        restTemplateMock = mock(RestTemplate.class);
        try {
            var field = NodeStartupRegisterService.class.getDeclaredField("restTemplate");
            field.setAccessible(true);
            field.set(nodeStartupRegisterService, restTemplateMock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            var cmField = NodeStartupRegisterService.class.getDeclaredField("cmPublicAddress");
            cmField.setAccessible(true);
            cmField.set(nodeStartupRegisterService, "localhost:8080");

            var nodeField = NodeStartupRegisterService.class.getDeclaredField("nodePublicAddress");
            nodeField.setAccessible(true);
            nodeField.set(nodeStartupRegisterService, "localhost:8081");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void registerAtStartup_sendsRequestToCM() throws Exception {
        when(restTemplateMock.postForEntity(anyString(), any(RegisterRequest.class), eq(Void.class)))
                .thenReturn(null);

        ApplicationArguments args = mock(ApplicationArguments.class);

        nodeStartupRegisterService.registerAtStartup();

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

        nodeStartupRegisterService.registerAtStartup();


        verify(restTemplateMock, times(10))
                .postForEntity(anyString(), any(RegisterRequest.class), eq(Void.class));
    }
}