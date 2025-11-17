package com.bcm.cluster_manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Service
public class BCMCronJob {

    private static final Logger logger = LoggerFactory.getLogger(BCMCronJob.class);

    @Autowired
    private HeartbeatService heartbeatService;


    @Scheduled(cron = "0 * * * * *") // every minute at second 0
    public void sendHeartBeatsEveryMinute() {
        System.out.println("CRON triggered every minute.");
        heartbeatService.heartbeatAll();
    }
}
