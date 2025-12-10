package com.bcm.cluster_manager.service;

import com.bcm.shared.service.NodeStartupRegisterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.RegisterRequest;

import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@Disabled("Skipping Spring context startup for now")
class ClusterManagerSelfRegisterTest {

    private NodeStartupRegisterService nodeRegistration;
    private RestTemplate restTemplate;
    private Environment environment;

    @BeforeEach
    void setup() {
        restTemplate = mock(RestTemplate.class);
        environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"cluster_manager"});
        nodeRegistration = new NodeStartupRegisterService(environment);

        try {
            // Inject mocked RestTemplate
            var restTemplateField = NodeStartupRegisterService.class.getDeclaredField("restTemplate");
            restTemplateField.setAccessible(true);
            restTemplateField.set(nodeRegistration, restTemplate);

            // Inject mocked Environment
            var environmentField = NodeStartupRegisterService.class.getDeclaredField("environment");
            environmentField.setAccessible(true);
            environmentField.set(nodeRegistration, environment);

            var cmAddressField = NodeStartupRegisterService.class.getDeclaredField("cmPublicAddress");
            cmAddressField.setAccessible(true);
            cmAddressField.set(nodeRegistration, "localhost:8080");

            var nodeAddressField = NodeStartupRegisterService.class.getDeclaredField("nodePublicAddress");
            nodeAddressField.setAccessible(true);
            nodeAddressField.set(nodeRegistration, "cluster-manager:8080");

            var attemptsField = NodeStartupRegisterService.class.getDeclaredField("maxAttempts");
            attemptsField.setAccessible(true);
            attemptsField.setInt(nodeRegistration, 1);

            var delayField = NodeStartupRegisterService.class.getDeclaredField("retryDelayMs");
            delayField.setAccessible(true);
            delayField.setLong(nodeRegistration, 0L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testRegisterClusterManagerAsNode() throws Exception {
        // Given
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(restTemplate.postForEntity(anyString(), any(RegisterRequest.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When
        nodeRegistration.registerAtStartup();

        // Then
        verify(restTemplate, times(1)).postForEntity(contains("/api/v1/cm/register"),
                argThat(req -> req instanceof RegisterRequest && ((RegisterRequest) req).getMode() == NodeMode.CLUSTER_MANAGER),
                eq(Void.class));
    }

    @Test
    void testRegisterClusterManagerAsNodeWithException() throws Exception {
        // Given
        ApplicationArguments args = mock(ApplicationArguments.class);
        doThrow(new RuntimeException("Test exception")).when(restTemplate)
                .postForEntity(anyString(), any(RegisterRequest.class), eq(Void.class));

        // When - Should not throw exception, just log the error
        nodeRegistration.registerAtStartup();

        // Then
        verify(restTemplate, times(1)).postForEntity(contains("/api/v1/cm/register"),
                argThat(req -> req instanceof RegisterRequest && ((RegisterRequest) req).getMode() == NodeMode.CLUSTER_MANAGER),
                eq(Void.class));
    }
}
