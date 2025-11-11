package com.bcm.cluster_manager;

import com.bcm.backup_manager.BackupManagerService;
import com.bcm.shared.model.BackupDTO;
import com.bcm.shared.model.NodeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/v1")
public class ClusterManagerController {

    @Autowired
    private ClusterManagerService clusterManagerService;

    @Autowired
    private BackupManagerService backupManagerService;


    @GetMapping("/nodes")
    public List<NodeDTO> getNodes() {
        return clusterManagerService.getAllNodes();
    }

    @GetMapping("/backups")
    public List<BackupDTO> getBackups() {
        return backupManagerService.getAllBackups();
    }
}
