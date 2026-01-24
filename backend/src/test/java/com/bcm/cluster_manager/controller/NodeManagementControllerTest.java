package com.bcm.cluster_manager.controller;

import com.bcm.cluster_manager.service.NodeManagementService;
import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@ExtendWith(MockitoExtension.class) // Nutzt Mockito ohne Spring Context
class NodeManagementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NodeManagementService nodeManagementService;

    @InjectMocks
    private NodeManagementController nodeManagementController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        // Hier bauen wir den Controller isoliert auf – ohne Security, ohne Context-Probleme!
        mockMvc = MockMvcBuilders.standaloneSetup(nodeManagementController).build();
    }

    @Test
    void register_shouldReturnJsonStatusOk() throws Exception {
        RegisterRequest request = new RegisterRequest("node1:8081", NodeMode.NODE, false);

        doNothing().when(nodeManagementService).registerNode(any(RegisterRequest.class));

        mockMvc.perform(post("/api/v1/cm/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    void register_shouldReturnJsonError_whenServiceFails() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Something wrong"))
                .when(nodeManagementService).registerNode(any());

        RegisterRequest request = new RegisterRequest("bad-node", NodeMode.NODE, false);

        mockMvc.perform(post("/api/v1/cm/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}