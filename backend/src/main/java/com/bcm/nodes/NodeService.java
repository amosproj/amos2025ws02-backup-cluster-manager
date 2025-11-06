package com.bcm.nodes;

import api.model.NodeClass;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class NodeService {

    public List<NodeClass> getAllNodes() {
        // Mock data for nodes
        return Arrays.asList(
            new NodeClass(1L, "Node A", "Active", LocalDateTime.now().minusDays(1)),
            new NodeClass(2L, "Node B", "Inactive", LocalDateTime.now().minusDays(2)),
            new NodeClass(3L, "Node C", "Active", LocalDateTime.now().minusDays(3))
        );
    }
}