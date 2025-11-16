package com.bcm.cluster_manager;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.NodeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/v1")
public class ClusterManagerController {

    @Autowired
    private ClusterManagerService clusterManagerService;

    @Autowired
    private BackupService backupService;


    @GetMapping("/nodes")
    public List<NodeDTO> getNodes(@RequestParam(required = false) Boolean active) {
        
        // Get all nodes
        List<NodeDTO> nodes = clusterManagerService.getAllNodes();
        
        // If active filter is true, return only nodes with status="Active"
        if (active != null && active) {
            return nodes.stream()
                .filter(node -> "Active".equalsIgnoreCase(node.getStatus()))
                .toList();
        }
        
        // Return all nodes if active is null or false
        return nodes;
    }

    @GetMapping("/backups")
    public List<BackupDTO> getBackups() {
        return backupService.getAllBackups();
    }
}
