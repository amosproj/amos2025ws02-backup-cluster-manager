    package com.bcm.cluster_manager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bcm.shared.model.api.NodeDTO;

//this is the implementation of the ClusterManagerService interface which can be removed when using a real database data source
@Service
public class ClusterManagerServiceImpl implements ClusterManagerService {

    public List<NodeDTO> findNodes(Boolean active, String search) {
        List<NodeDTO> nodes = getAllNodes();

        // Filter by active status
        if (Boolean.TRUE.equals(active)) {
            nodes = nodes.stream()
                .filter(node -> "Active".equalsIgnoreCase(node.getStatus()))
                .toList();
        }

        // Filter by search (match by id or name)
        if (search != null && !search.isBlank()) {
            final String term = search.trim().toLowerCase();
            final boolean isNumeric = term.chars().allMatch(Character::isDigit);

            nodes = nodes.stream().filter(node -> {
                boolean nameMatch = node.getName() != null && node.getName().toLowerCase().contains(term);
                boolean idMatch = false;
                if (isNumeric && node.getId() != null) {
                    // Use equals() for exact id match; use contains() for partial match
                    idMatch = node.getId().toString().contains(term);
                }
                return nameMatch || idMatch;
            }).toList();
        }
        return nodes;
    }

    public List<NodeDTO> getAllNodes() {
        // Mock data for nodes
        return Arrays.asList(
                new NodeDTO(1L, "Node A", "Active", LocalDateTime.now().minusDays(1)),
                new NodeDTO(2L, "Node B", "Inactive", LocalDateTime.now().minusDays(2)),
                new NodeDTO(3L, "Node C", "Active", LocalDateTime.now().minusDays(3))
        );
    }
}