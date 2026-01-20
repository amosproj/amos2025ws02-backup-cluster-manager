package com.bcm.cluster_manager.controller;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cm/cache")
public class CacheInspectController {

    @Autowired
    private CacheManager cacheManager;

    @GetMapping("/inspect")
    public Map<String, Object> inspectCaches() {
        Map<String, Object> result = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);

            if (cache instanceof CaffeineCache) {
                CaffeineCache caffeineCache = (CaffeineCache) cache;
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                        caffeineCache.getNativeCache();

                // Get all entries
                Map<Object, Object> entries = nativeCache.asMap();

                // Get stats
                CacheStats stats = nativeCache.stats();

                result.put(cacheName, Map.of(
                        "size", entries.size(),
                        "entries", entries,
                        "hitRate", stats.hitRate(),
                        "hitCount", stats.hitCount(),
                        "missCount", stats.missCount(),
                        "evictionCount", stats.evictionCount()
                ));
            }
        });

        return result;
    }

    @GetMapping("/{cacheName}")
    public Map<String, Object> inspectCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);

        if (cache instanceof CaffeineCache) {
            CaffeineCache caffeineCache = (CaffeineCache) cache;
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                    caffeineCache.getNativeCache();

            return Map.of(
                    "name", cacheName,
                    "size", nativeCache.asMap().size(),
                    "entries", nativeCache.asMap(),
                    "stats", nativeCache.stats()
            );
        }

        return Map.of("error", "Cache not found");
    }

}
