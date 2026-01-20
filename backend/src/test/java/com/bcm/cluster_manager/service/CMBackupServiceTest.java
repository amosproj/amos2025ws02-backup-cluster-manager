package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.*;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.sort.SortOrder;
import com.bcm.shared.repository.BackupMapper;
import com.bcm.shared.service.BackupService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CMBackupServiceTest {

    @Mock private RegistryService registryService;
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec uriSpec;
    @Mock private WebClient.RequestHeadersSpec headersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    private CacheManager cacheManager;
    private CMBackupService service;
    static class TestFilter extends Filter {}
    private EventPollingService pollingService;

    @BeforeEach
    void setUp() {
        cacheManager = new org.springframework.cache.concurrent.ConcurrentMapCacheManager("backupPages");

        // Mock WebClient.Builder
        WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);
        when(webClientBuilder.build()).thenReturn(webClient);


        service = new CMBackupService(registryService, webClientBuilder, null, cacheManager);
        pollingService = new EventPollingService(webClientBuilder);

        injectField(pollingService, "registryService", registryService);
        injectField(pollingService, "cacheManager", cacheManager);
        injectField(pollingService, "webClient", webClient);


    }
    private void injectField(Object target, String fieldName, Object value) {
        try {
            var field = EventPollingService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldCachePageResults() {
        // Given
        mockBackupResponse(List.of(backup(1L), backup(2L)));

        // When - First call
        StepVerifier.create(service.getDBItems(1, 15, filter()))
                .expectNextCount(1)
                .verifyComplete();

        // Then - Second call uses cache (no WebClient call)
        clearInvocations(webClient);
        StepVerifier.create(service.getDBItems(1, 15, filter()))
                .expectNextCount(1)
                .verifyComplete();

        verify(webClient, never()).get();
    }

    @Test
    void shouldCacheDifferentPagesAndFiltersSeparately() {
        // Create enough backups for multiple pages
        List<BackupDTO> backups = List.of(
                backup(1L), backup(2L), backup(3L),
                backup(4L), backup(5L), backup(6L),
                backup(7L), backup(8L), backup(9L),
                backup(10L), backup(11L), backup(12L),
                backup(13L), backup(14L), backup(15L),
                backup(16L), backup(17L), backup(18L)
        );
        mockBackupResponse(backups);

        Filter filter1 = filter();
        Filter filter2 = filter();
        filter2.setSearch("Backup");

        service.getDBItems(1, 15, filter1).block();
        service.getDBItems(2, 15, filter1).block();
        service.getDBItems(1, 15, filter2).block();

        var cache = cacheManager.getCache("backupPages");
        assertThat(cache.get("page-1-size-15-sort-id-order-ASC")).isNotNull();
        assertThat(cache.get("page-2-size-15-sort-id-order-ASC")).isNotNull();
        assertThat(cache.get("page-1-size-15-search-Backup-sort-id-order-ASC")).isNotNull();
    }

    @Test
    void shouldCreateStableCacheKeysRegardlessOfFilterOrder() {
        // Given
        mockBackupResponse(List.of(backup(1L)));

        Filter filter1 = filter();
        filter1.setFilters(new HashSet<>(Set.of("COMPLETED", "FAILED")));

        Filter filter2 = filter();
        filter2.setFilters(new HashSet<>(Set.of("FAILED", "COMPLETED")));

        // When
        service.getDBItems(1, 15, filter1).block();
        clearInvocations(webClient);
        service.getDBItems(1, 15, filter2).block();

        // Then - Second call should hit cache (filters sorted)
        verify(webClient, never()).get();
    }

    @Test
    void shouldCacheFilteredResults() {
        // Given
        mockBackupResponse(List.of(
                backup(1L, BackupState.COMPLETED),
                backup(2L, BackupState.FAILED),
                backup(3L, BackupState.COMPLETED)
        ));

        Filter filter = filter();
        filter.setFilters(new HashSet<>(Set.of("COMPLETED")));

        // When
        var result = service.getDBItems(1, 15, filter).block();

        // Then - Only filtered results cached
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(b -> b.getState() == BackupState.COMPLETED);

        var cached = cacheManager.getCache("backupPages")
                .get("page-1-size-15-filters-COMPLETED-sort-id-order-ASC", List.class);
        assertThat(cached).hasSize(2);
    }

    @Test
    void shouldInvalidateCorrectCacheBasedOnEventType() {
        Cache backupCache = cacheManager.getCache("backupPages");
        backupCache.put("key", "data");

        pollingService.processEvents(List.of(event(CacheInvalidationType.BACKUP_CREATED)));

        assertThat(backupCache.get("key")).isNull();
    }

    // Helper methods
    private void mockBackupResponse(List<BackupDTO> backups) {
        NodeDTO node = new NodeDTO();
        node.setAddress("node1:8080");

        when(registryService.getActiveAndManagedNodes()).thenReturn(List.of(node));
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(BackupDTO.class)).thenReturn(Flux.fromIterable(backups));
    }
    private CacheEventDTO event(CacheInvalidationType type) {
        CacheEventDTO e = new CacheEventDTO();
        e.setId(System.currentTimeMillis());
        e.setType(type);
        e.setEntityId(1L);
        e.setTimestamp(Instant.now());
        return e;
    }

    private Filter filter() {
        Filter f = new TestFilter();
        f.setSortBy("id");
        f.setSortOrder(SortOrder.ASC);
        return f;
    }

    private BackupDTO backup(Long id) {
        return backup(id, BackupState.COMPLETED);
    }

    private BackupDTO backup(Long id, BackupState state) {
        BackupDTO b = new BackupDTO();
        b.setId(id);
        b.setClientId(1L);
        b.setTaskId(1L);
        b.setName("Backup " + id);
        b.setState(state);
        b.setSizeBytes(1000L);
        b.setStartTime(LocalDateTime.now());
        b.setCreatedAt(LocalDateTime.now());
        return b;
    }

    /*
    @Test
    void createBackup_withActiveNodes_shouldCreateQueuedBackupAndForwardSavedDto() {
        // Arrange
        List<NodeDTO> nodes = List.of(
                new NodeDTO(1L, "node1:8081", "node1:8081", NodeStatus.ACTIVE, NodeMode.NODE, LocalDateTime.now()),
                new NodeDTO(2L, "node2:8082", "node2:8082", NodeStatus.ACTIVE, NodeMode.NODE, LocalDateTime.now())
        );
        when(registryService.getActiveNodes()).thenReturn(nodes);

        BackupDTO saved = new BackupDTO(
                42L,
                1L,
                1L,
                "Backup-1",
                BackupState.QUEUED,
                100L,
                null,
                null,
                LocalDateTime.now(),
                List.of("node1:8081", "node2:8082")
        );

        BackupService spyService = spy(backupService);
        doReturn(saved).when(spyService).store(any());

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act
        BackupDTO returned = spyService.createBackup(request);

        // Assert
        assertThat(returned.getId()).isNull();
        assertThat(returned.getClientId()).isEqualTo(1L);
        assertThat(returned.getTaskId()).isEqualTo(1L);
        assertThat(returned.getSizeBytes()).isEqualTo(100L);
        assertThat(returned.getState()).isEqualTo(BackupState.QUEUED);
        assertThat(returned.getReplicationNodes())
                .containsExactlyInAnyOrder("node1:8081", "node2:8082");

        ArgumentCaptor<BackupDTO> toStoreCaptor = ArgumentCaptor.forClass(BackupDTO.class);
        verify(spyService, times(1)).store(toStoreCaptor.capture());
        BackupDTO toStore = toStoreCaptor.getValue();
        assertThat(toStore.getId()).isNull();
        assertThat(toStore.getState()).isEqualTo(BackupState.QUEUED);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BackupDTO> forwardedCaptor = ArgumentCaptor.forClass(BackupDTO.class);
        verify(restTemplate, times(1))
                .postForEntity(urlCaptor.capture(), forwardedCaptor.capture(), eq(Void.class));

        assertThat(urlCaptor.getValue())
                .isEqualTo("http://node2:8082/api/v1/bm/backups");

        BackupDTO forwarded = forwardedCaptor.getValue();
        assertThat(forwarded.getId()).isEqualTo(42L);
        assertThat(forwarded.getClientId()).isEqualTo(1L);
        assertThat(forwarded.getTaskId()).isEqualTo(1L);
        assertThat(forwarded.getState()).isEqualTo(BackupState.QUEUED);
    }

    @Test
    void createBackup_withNoActiveNodes_shouldThrowAndNotCallStoreNorBm() {
        // Arrange
        when(registryService.getActiveNodes()).thenReturn(List.of());
        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act & Assert
        assertThatThrownBy(() -> backupService.createBackup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No active nodes available");

        verifyNoInteractions(backupMapper);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void createBackup_whenForwardingFails_shouldStillHaveStoredBackupAndThrow() {
        // Arrange
        List<NodeDTO> nodes = List.of(
                new NodeDTO(1L, "node2:8082", "node2:8082", NodeStatus.ACTIVE, NodeMode.NODE, LocalDateTime.now())
        );
        when(registryService.getActiveNodes()).thenReturn(nodes);

        BackupDTO saved = new BackupDTO(
                77L, 1L, 1L, "Backup-1",
                BackupState.QUEUED, 100L,
                null, null, LocalDateTime.now(),
                List.of("node2:8082")
        );

        BackupService spyService = spy(backupService);
        doReturn(saved).when(spyService).store(any());

        doThrow(new RuntimeException("Connection refused"))
                .when(restTemplate).postForEntity(anyString(), any(), eq(Void.class));

        CreateBackupRequest request = new CreateBackupRequest(1L, 1L, 100L);

        // Act & Assert
        assertThatThrownBy(() -> spyService.createBackup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Connection refused");

        verify(spyService, times(1)).store(any());
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(Void.class));
    }

     */
}
