package com.bcm.cluster_manager;


import com.bcm.shared.model.api.NodeDTO;
import java.util.List;


public interface ClusterManagerService {
    List<NodeDTO> getAllNodes();
    List<NodeDTO> findNodes(Boolean active, String search);
}
