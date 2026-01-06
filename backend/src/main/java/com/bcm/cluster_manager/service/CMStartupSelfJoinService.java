package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@Profile("cluster_manager")
public class CMStartupSelfJoinService {
    private static final Logger log = LoggerFactory.getLogger(CMStartupSelfJoinService.class);

    private final NodeManagementService nodeManagementService;

    @Value("${application.cm.public-address:localhost:8080}")
    private String cmPublicAddress;

    @Value("${application.node.public-address:localhost:8081}")
    private String nodePublicAddress;

    private boolean hasJoined = false;

    public CMStartupSelfJoinService(NodeManagementService nodeManagementService) {
        this.nodeManagementService = nodeManagementService;
    }

    @Scheduled(fixedDelay = 5000)
    public void joinCluster() {
        if (hasJoined) return;

        NodeMode nodeType = NodeMode.CLUSTER_MANAGER;
        RegisterRequest req = new RegisterRequest(nodePublicAddress, nodeType);

        String protocol = cmPublicAddress.startsWith("http") ? "" : "http://";
        String cmRegisterUrl = protocol + cmPublicAddress + "/api/v1/cm/register";
        try {
            log.info("Attempting registration at CM: {} as {}", cmRegisterUrl, nodeType);

            nodeManagementService.registerNode(req);

            log.info("SUCCESS: Registered self as {}", nodeType);
            hasJoined = true;

        } catch (Exception e) {
            log.warn("Registration failed (CM might be starting up): {}", e.getMessage());
        }
    }
}
