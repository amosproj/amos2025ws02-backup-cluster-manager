package com.bcm.cluster_manager.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Configure and return the desired CacheManager implementation
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Backup list cache
        cacheManager.registerCustomCache("backupsList",
                Caffeine.newBuilder()
                        .expireAfterWrite(2, TimeUnit.MINUTES)
                        .maximumSize(100)
                        .recordStats()
                        .build()
        );

        // Backup count cache
        cacheManager.registerCustomCache("backupsCount",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(50)
                        .recordStats()
                        .build()
        );

        return cacheManager;
    }


}
