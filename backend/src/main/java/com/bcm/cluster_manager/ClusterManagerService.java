package com.bcm.cluster_manager;


import com.bcm.shared.model.NodeDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ClusterManagerService {


    public List<NodeDTO> getAllNodes() {
        // Mock data for nodes
        return Arrays.asList(
                new NodeDTO(1L, "Node A", "Active", LocalDateTime.now().minusDays(1)),
                new NodeDTO(2L, "Node B", "Inactive", LocalDateTime.now().minusDays(2)),
                new NodeDTO(3L, "Node C", "Active", LocalDateTime.now().minusDays(3))
        );
    }
}
