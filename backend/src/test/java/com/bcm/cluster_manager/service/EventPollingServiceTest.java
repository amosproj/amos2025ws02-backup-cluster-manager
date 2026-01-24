package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.CacheEventDTO;
import com.bcm.shared.model.api.CacheInvalidationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPollingServiceTest {

    @Mock private RegistryService registryService;
    @Mock private CacheManager cacheManager;
    @Mock private WebClient webClient;
    @Mock private WebClient.Builder webClientBuilder;

    private EventPollingService service;
    private Cache backupCache;
    private Cache clientCache;
    private Cache taskCache;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        service = new EventPollingService(webClientBuilder);

        backupCache = new ConcurrentMapCache("backupPages");
        clientCache = new ConcurrentMapCache("clientPages");
        taskCache = new ConcurrentMapCache("taskPages");

        lenient().when(cacheManager.getCache("backupPages")).thenReturn(backupCache);
        lenient().when(cacheManager.getCache("clientPages")).thenReturn(clientCache);
        lenient().when(cacheManager.getCache("taskPages")).thenReturn(taskCache);

        injectField(service, "registryService", registryService);
        injectField(service, "cacheManager", cacheManager);
    }

    @Test
    void processEvents_withBackupEvents_shouldInvalidateBackupCache() {
        backupCache.put("key1", "data1");
        backupCache.put("key2", "data2");

        List<CacheEventDTO> events = List.of(
                event(CacheInvalidationType.BACKUP_CREATED),
                event(CacheInvalidationType.BACKUP_UPDATED)
        );

        service.processEvents(events);

        assertThat(backupCache.get("key1")).isNull();
        assertThat(backupCache.get("key2")).isNull();
    }

    @Test
    void processEvents_withClientEvents_shouldInvalidateClientCache() {
        clientCache.put("key1", "data1");

        List<CacheEventDTO> events = List.of(
                event(CacheInvalidationType.CLIENT_CREATED),
                event(CacheInvalidationType.CLIENT_DELETED)
        );

        service.processEvents(events);

        assertThat(clientCache.get("key1")).isNull();
    }

    @Test
    void processEvents_withTaskEvents_shouldInvalidateTaskCache() {
        taskCache.put("key1", "data1");

        List<CacheEventDTO> events = List.of(
                event(CacheInvalidationType.TASK_CREATED),
                event(CacheInvalidationType.TASK_UPDATED)
        );

        service.processEvents(events);

        assertThat(taskCache.get("key1")).isNull();
    }

    @Test
    void processEvents_withMultipleEventTypes_shouldInvalidateAllRelevantCaches() {
        backupCache.put("backupKey", "backupData");
        clientCache.put("clientKey", "clientData");
        taskCache.put("taskKey", "taskData");

        List<CacheEventDTO> events = List.of(
                event(CacheInvalidationType.BACKUP_CREATED),
                event(CacheInvalidationType.CLIENT_UPDATED),
                event(CacheInvalidationType.TASK_DELETED)
        );

        service.processEvents(events);

        assertThat(backupCache.get("backupKey")).isNull();
        assertThat(clientCache.get("clientKey")).isNull();
        assertThat(taskCache.get("taskKey")).isNull();
    }

    @Test
    void processEvents_withEmptyList_shouldNotInvalidateAnyCache() {
        backupCache.put("key1", "data1");
        clientCache.put("key2", "data2");
        taskCache.put("key3", "data3");

        service.processEvents(List.of());

        assertThat(backupCache.get("key1")).isNotNull();
        assertThat(clientCache.get("key2")).isNotNull();
        assertThat(taskCache.get("key3")).isNotNull();
    }

    @Test
    void processEvents_withNonExistentCache_shouldHandleGracefully() {
        when(cacheManager.getCache("backupPages")).thenReturn(null);

        List<CacheEventDTO> events = List.of(event(CacheInvalidationType.BACKUP_CREATED));

        service.processEvents(events);

        verify(cacheManager, times(1)).getCache("backupPages");
    }

    @Test
    void processEvents_withSingleBackupEvent_shouldInvalidateBackupCache() {
        backupCache.put("key", "data");

        List<CacheEventDTO> events = List.of(event(CacheInvalidationType.BACKUP_DELETED));

        service.processEvents(events);

        assertThat(backupCache.get("key")).isNull();
    }

    private CacheEventDTO event(CacheInvalidationType type) {
        CacheEventDTO e = new CacheEventDTO();
        e.setId(System.currentTimeMillis());
        e.setType(type);
        e.setEntityId(1L);
        e.setTimestamp(Instant.now());
        return e;
    }

    private void injectField(Object target, String fieldName, Object value) {
        try {
            Field field = EventPollingService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
