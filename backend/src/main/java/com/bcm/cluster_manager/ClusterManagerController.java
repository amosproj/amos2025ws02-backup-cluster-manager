package com.bcm.cluster_manager;

import api.model.NodeDTO;
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


    @GetMapping("/nods")
    public List<NodeDTO> getNodes() {
        return clusterManagerService.getAllNodes();
    }
}
