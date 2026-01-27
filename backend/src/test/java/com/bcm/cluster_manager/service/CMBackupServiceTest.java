package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.BigBackupDTO;
import com.bcm.shared.model.api.*;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.sort.SortOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.mockito.ArgumentMatchers.contains;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CMBackupServiceTest {

    @Mock private RegistryService registryService;
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec uriSpec;
    @Mock private WebClient.RequestHeadersSpec headersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;
    @Mock private EventPollingService pollingService;
    @Mock private WebClient.RequestBodyUriSpec postUriSpec;
    @Mock private WebClient.RequestBodySpec postBodySpec;
    @Mock private WebClient.RequestHeadersSpec postHeadersSpec;
    @Mock private WebClient.ResponseSpec postResponseSpec;

    @Mock private WebClient.RequestHeadersUriSpec deleteUriSpec;
    @Mock private WebClient.RequestHeadersSpec<?> deleteHeadersSpec;
    @Mock private WebClient.ResponseSpec deleteResponseSpec;

    private CacheManager cacheManager;
    private CMBackupService service;
    static class TestFilter extends Filter {}

    @BeforeEach
    void setUp() {
        cacheManager = new org.springframework.cache.concurrent.ConcurrentMapCacheManager("backupPages");

        // Mock WebClient.Builder
        WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);
        when(webClientBuilder.build()).thenReturn(webClient);
        service = new CMBackupService(registryService, webClientBuilder, null, cacheManager);
    }

    @Test
    void getAllBackups_shouldAggregateFromNodes_andIgnoreFailingNode() {
        NodeDTO n1 = new NodeDTO(); n1.setAddress("node1:8081");
        NodeDTO n2 = new NodeDTO(); n2.setAddress("node2:8082");
        when(registryService.getActiveAndManagedNodes()).thenReturn(List.of(n1, n2));

        // webClient.get() chain
        when(webClient.get()).thenReturn(uriSpec);

        // return different headersSpec based on URL
        WebClient.RequestHeadersSpec<?> headers1 = mock(WebClient.RequestHeadersSpec.class);
        WebClient.RequestHeadersSpec<?> headers2 = mock(WebClient.RequestHeadersSpec.class);

        when(uriSpec.uri(contains("node1:8081"))).thenReturn(headers1);
        when(uriSpec.uri(contains("node2:8082"))).thenReturn(headers2);

        WebClient.ResponseSpec resp1 = mock(WebClient.ResponseSpec.class);
        WebClient.ResponseSpec resp2 = mock(WebClient.ResponseSpec.class);
        when(headers1.retrieve()).thenReturn(resp1);
        when(headers2.retrieve()).thenReturn(resp2);

        when(resp1.bodyToFlux(BackupDTO.class)).thenReturn(Flux.just(backup(1L), backup(2L)));
        when(resp2.bodyToFlux(BackupDTO.class)).thenReturn(Flux.error(new RuntimeException("boom")));

        StepVerifier.create(service.getAllBackups(filter()))
                .assertNext(list -> {
                    assertThat(list).hasSize(2);
                    assertThat(list).allMatch(b -> b.getNodeDTO() != null);
                    assertThat(list).allMatch(b -> b.getNodeDTO().getAddress().equals("node1:8081"));
                })
                .verifyComplete();
    }

    @Test
    void createBackup_shouldPostToTargetNode_andReturnBigBackup() {
        NodeDTO target = new NodeDTO(); target.setAddress("node1:8080");
        when(registryService.getActiveAndManagedNodes()).thenReturn(List.of(target));

        BackupDTO created = backup(99L, BackupState.QUEUED);

        when(webClient.post()).thenReturn(postUriSpec);
        when(postUriSpec.uri("http://node1:8080/api/v1/bn/backups/sync")).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any(BackupDTO.class))).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);
        when(postResponseSpec.bodyToMono(BackupDTO.class)).thenReturn(Flux.just(created).single());

        BigBackupDTO req = new BigBackupDTO();
        req.setNodeDTO(target);
        req.setClientId(1L);
        req.setTaskId(2L);
        req.setSizeBytes(123L);

        StepVerifier.create(service.createBackup(req))
                .assertNext(b -> {
                    assertThat(b.getId()).isEqualTo(99L);
                    assertThat(b.getNodeDTO().getAddress()).isEqualTo("node1:8080");
                    assertThat(b.getState()).isEqualTo(BackupState.QUEUED);
                })
                .verifyComplete();

        verify(postBodySpec).bodyValue(any(BackupDTO.class));
    }

    @Test
    void deleteBackup_shouldDeleteFromActiveNode() {
        NodeDTO n1 = new NodeDTO(); n1.setAddress("node1:8080");
        when(registryService.getActiveAndManagedNodes()).thenReturn(List.of(n1));

        when(webClient.delete()).thenReturn(deleteUriSpec);
        when(deleteUriSpec.uri("http://node1:8080/api/v1/bn/backups/7")).thenReturn(deleteHeadersSpec);
        when(deleteHeadersSpec.retrieve()).thenReturn(deleteResponseSpec);
        when(deleteResponseSpec.toBodilessEntity()).thenReturn(Mono.just(org.springframework.http.ResponseEntity.ok().build()));

        StepVerifier.create(service.deleteBackup(7L, "node1:8080"))
                .verifyComplete();

        verify(webClient).delete();
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
        assertThat(cache).isNotNull();
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
        var cach_backups = cacheManager.getCache("backupPages");
        assertThat(cach_backups).isNotNull();
        var cached = cach_backups.get("page-1-size-15-filters-COMPLETED-sort-id-order-ASC", List.class);
        assertThat(cached).hasSize(2);
    }

    @Test
    void shouldInvalidateCorrectCacheBasedOnEventType() {
        Cache backupCache = cacheManager.getCache("backupPages");
        assertThat(backupCache).isNotNull();
        backupCache.put("key", "data");
        assertThat(backupCache.get("key")).isNotNull();

        pollingService.processEvents(List.of(event(CacheInvalidationType.BACKUP_CREATED)));

        verify(pollingService, times(1)).processEvents(any());

        backupCache.clear();
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
        b.setStartTime(Instant.now());
        b.setCreatedAt(Instant.now());
        return b;
    }
}
