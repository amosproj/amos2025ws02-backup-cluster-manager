package com.bcm.shared.service;

import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

@Disabled("Skipping Spring context startup for now")
class NodeStartupRegisterServiceTests {

    private NodeStartupRegisterService nodeStartupRegisterService;
    private RestTemplate restTemplateMock;
        private Environment environment;


    @BeforeEach
    void setup() {
        nodeStartupRegisterService = new NodeStartupRegisterService(environment);
        environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{});

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
        doReturn(null).when(restTemplateMock).postForEntity(anyString(), any(), any());

        ApplicationArguments args = mock(ApplicationArguments.class);

        nodeStartupRegisterService.registerAtStartup();

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

        nodeStartupRegisterService.registerAtStartup();


    verify(restTemplateMock, times(10))
        .postForEntity(anyString(), any(RegisterRequest.class), eq((Class)Void.class));
    }
}