    package com.bcm.cluster_manager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bcm.cluster_manager.service.ClusterManagerService;
import com.bcm.shared.model.api.NodeDTO;

//this is the implementation of the ClusterManagerService interface which can be removed when using a real database data source
@Service
public class ClusterManagerServiceImpl implements ClusterManagerService {

    public List<NodeDTO> findNodes(Boolean active, String search, String sortBy, String sortOrder) {
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
        
        // Sorting logic can be added here based on sortBy and sortOrder parameters
        if (sortBy != null && !sortBy.isBlank()) {
            Comparator<NodeDTO> comparator = getComparator(sortBy);
            
            if (comparator != null) {
                // Reverse if descending order
                if ("desc".equalsIgnoreCase(sortOrder)) {
                    comparator = comparator.reversed();
                }
                
                nodes = nodes.stream()
                    .sorted(comparator)
                    .toList();
            }
        }

        return nodes;
    }

    private Comparator<NodeDTO> getComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "name" -> Comparator.comparing(NodeDTO::getName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "status" -> Comparator.comparing(NodeDTO::getStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            case "createdat" -> Comparator.comparing(NodeDTO::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo));
            case "id" -> Comparator.comparing(NodeDTO::getId, Comparator.nullsLast(Long::compareTo));
            default -> null;
        };
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