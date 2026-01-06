package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.SyncDTO;
import com.bcm.shared.model.database.User;
import com.bcm.shared.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SyncService {
    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    @Autowired
    private RegistryService registry;

    private final UserService userService;

    public SyncService(@Qualifier("userServiceCM") UserService userService) {
        this.userService = userService;
    }

    private final RestTemplate rest = new RestTemplate();

    public void syncNodes() {
        List<User> cmUsers = userService.getAllUsers();
        SyncDTO dto = new SyncDTO();
        dto.setCmUsers(cmUsers);
        registry.getActiveAndManagedNodes().forEach(node -> asyncPush(node.getAddress(), dto));
    }

    @Async
    protected CompletableFuture<Void> asyncPush(String address, SyncDTO dto) {
        String url = "http://" + address + "/api/v1/sync";
        try {
            rest.postForEntity(url, dto, Void.class);
            logger.info("Pushed users to {}", address);
        } catch (Exception e) {
            logger.warn("Failed to push users to {}: {}", address, e.toString());
        }
        return CompletableFuture.completedFuture(null);
    }
}