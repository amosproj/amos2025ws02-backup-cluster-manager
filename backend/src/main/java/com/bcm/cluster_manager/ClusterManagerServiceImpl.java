    package com.bcm.cluster_manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bcm.cluster_manager.service.ClusterManagerService;
import com.bcm.shared.filter.FilterProvider;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.sort.NodeComparators;
import com.bcm.shared.sort.SortProvider;

//this is the implementation of the ClusterManagerService interface which can be removed when using a real database data source
@Service
public class ClusterManagerServiceImpl implements ClusterManagerService{
    public List<NodeDTO> exampleNodes;

    public ClusterManagerServiceImpl(){
        int numBackups = 1000;
        List<NodeDTO> list = new ArrayList<>();
        for (int i = 1; i <= numBackups; i++) {
            list.add(new NodeDTO(
                    (long) i,
                    "Node " + i,
                    "www.google.com",
                    "active",
                    LocalDateTime.now().minusDays(i)
            ));
        }
        exampleNodes = list;
    }

    public List<NodeDTO> findNodes(Boolean active, String search, String sortBy, String sortOrder) {
        List<NodeDTO> nodes = getAllNodes();

        // Filter by active status
        nodes = FilterProvider.filterByActive(nodes, active, NodeDTO::getStatus);

        // Filter by search (match by id or name)
        nodes = FilterProvider.filterBySearchFields(nodes, search, List.of(
            n -> n.getId() == null ? "" : n.getId().toString(),
            NodeDTO::getName
        ));
        
        /// Sort using SortProvider
        nodes = SortProvider.sort(nodes, sortBy, sortOrder, NodeComparators.COMPARATORS);

        return nodes;
    }
    
    public List<NodeDTO> getAllNodes() {
        // Mock data for nodes
        return exampleNodes;
    }
}
