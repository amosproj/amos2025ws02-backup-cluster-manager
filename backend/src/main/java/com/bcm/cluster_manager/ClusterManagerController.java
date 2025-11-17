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
    public List<NodeDTO> getNodes(
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortOrder
    ) {
        return clusterManagerService.findNodes(active, search , sortBy, sortOrder);
    }

    @GetMapping("/backups")
    public List<BackupDTO> getBackups() {
        return backupService.getAllBackups();
    }
}
