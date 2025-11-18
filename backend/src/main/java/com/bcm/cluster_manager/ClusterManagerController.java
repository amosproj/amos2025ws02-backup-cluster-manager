package com.bcm.cluster_manager;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/v1")
public class ClusterManagerController {

    @Autowired
    private ClusterManagerService clusterManagerService;

    @Autowired
    private BackupService backupService;


    @GetMapping("/nodes")
    public PaginationResponse<NodeDTO> getNodes(PaginationRequest pagination){
        return clusterManagerService.getPaginatedItems(
                pagination.getPage(),
                pagination.getItemsPerPage()
        );
    }

    @GetMapping("/backups")
    public PaginationResponse<BackupDTO> getBackups(PaginationRequest pagination) {
        return backupService.getPaginatedItems(
                pagination.getPage(),
                pagination.getItemsPerPage()
        );
    }
}
