package com.bcm.cluster_manager.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Configure and return the desired CacheManager implementation
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Cache for counts
        cacheManager.registerCustomCache("backupPages",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .expireAfterAccess(2, TimeUnit.MINUTES)
                        .maximumSize(100)
                        .recordStats()
                        .build()
        );

        // Cache for client pages
        cacheManager.registerCustomCache("clientPages",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .expireAfterAccess(2, TimeUnit.MINUTES)
                        .maximumSize(100)
                        .recordStats()
                        .build()
        );

        // Cache for task pages
        cacheManager.registerCustomCache("taskPages",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .expireAfterAccess(2, TimeUnit.MINUTES)
                        .maximumSize(100)
                        .recordStats()
                        .build()
        );
        return cacheManager;
    }

}
