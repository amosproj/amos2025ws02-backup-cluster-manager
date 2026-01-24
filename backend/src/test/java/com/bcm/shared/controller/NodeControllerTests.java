package com.bcm.shared.controller;

import com.bcm.shared.config.security.NoSecurityConfig;
import com.bcm.shared.model.api.SyncDTO;
import com.bcm.shared.model.database.User;
import com.bcm.shared.service.NodeControlService;
import com.bcm.shared.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = NodeController.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {NodeController.class, NoSecurityConfig.class})
class NodeControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private NodeControlService nodeControlService;

    @Test
    void exampleEndpoint_returnsString() {
        webTestClient.get()
                .uri("/api/v1/example")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Here is a string");
    }

    @Test
    void pingEndpoint_returnsPong() {
        webTestClient.get()
                .uri("/api/v1/ping")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("pong");
    }

    @Test
    void syncEndpoint_callsReplaceUsers() {
        SyncDTO dto = new SyncDTO(List.of(new User(), new User()));

        webTestClient.post()
                .uri("/api/v1/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(userService).replaceUsersWithCMUsers(anyList());
    }

    @Test
    void statusEndpoint_returnsManagedMode() {
        when(nodeControlService.isManagedMode()).thenReturn(true);

        webTestClient.get()
                .uri("/api/v1/status")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.managedMode").isEqualTo(true);
    }
}