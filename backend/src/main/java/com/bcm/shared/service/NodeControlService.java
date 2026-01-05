package com.bcm.shared.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class NodeControlService {

    private static final Logger logger = LoggerFactory.getLogger(NodeControlService.class);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private ApplicationContext applicationContext;

    private boolean managedMode = true;

    public void shutdown() {
        logger.info("Received shutdown command. Initiating graceful shutdown...");
        scheduler.schedule(() -> {
            logger.info("Shutting down application...");
            SpringApplication.exit(applicationContext, () -> 0);
            System.exit(0);
        }, 2, TimeUnit.SECONDS);
    }

    public void restart() {
        logger.info("Received restart command. Initiating restart...");
        scheduler.schedule(() -> {
            logger.info("Restarting application...");
            // Exit with code 1 to signal container orchestrator to restart
            SpringApplication.exit(applicationContext, () -> 1);
            System.exit(1);
        }, 2, TimeUnit.SECONDS);
    }

    public void disableManagedMode() {
        logger.info("Received disable managed mode command. Node will no longer be managed by cluster manager.");
        this.managedMode = false;
    }

    public void enableManagedMode() {
        logger.info("Enabling managed mode. Node will be managed by cluster manager.");
        this.managedMode = true;
    }

    public boolean isManagedMode() {
        return managedMode;
    }
}

