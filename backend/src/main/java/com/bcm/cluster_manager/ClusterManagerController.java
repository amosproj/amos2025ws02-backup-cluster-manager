package com.bcm.cluster_manager;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/clusterManager")
public class ClusterManagerController {

    @GetMapping("/test")
    public String test(){
        return "This is a cluster manager endpoint";
    }
}
