package com.bcm.cluster_manager.service;


import com.bcm.shared.model.api.NodeDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ClusterManagerService {


    public List<NodeDTO> getAllNodes() {
        // Mock data for nodes
        return Arrays.asList(
                new NodeDTO(1L, "Node A", "10.100.179.80:9300", "Active", LocalDateTime.now().minusDays(1)),
                new NodeDTO(2L, "Node B", "10.100.179.81:2030" , "Inactive", LocalDateTime.now().minusDays(2)),
                new NodeDTO(3L, "Node C", "10.100.179.82:3333", "Active", LocalDateTime.now().minusDays(3))
        );
    }
}
