package com.bcm.cluster_manager;

import com.bcm.cluster_manager.service.HeartbeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled job that triggers heartbeat checks for all registered nodes every minute.
 */
@Service
public class BCMCronJob {

    private static final Logger logger = LoggerFactory.getLogger(BCMCronJob.class);

    @Autowired
    private HeartbeatService heartbeatService;


    /**
     * Runs every minute; sends heartbeats to all nodes and updates their status.
     */
    @Scheduled(cron = "0 * * * * *") // every minute at second 0
    public void sendHeartBeatsEveryMinute() {
        System.out.println("CRON triggered every minute.");
        heartbeatService.heartbeatAll();
    }
}
