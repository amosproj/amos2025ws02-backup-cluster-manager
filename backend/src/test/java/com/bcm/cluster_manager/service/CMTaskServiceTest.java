package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.BigTaskDTO;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.model.database.TaskFrequency;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.sort.SortOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CMTaskServiceTest {

    @Mock private RegistryService registryService;
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec uriSpec;
    @Mock private WebClient.RequestHeadersSpec headersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    private CacheManager cacheManager;
    private CMTaskService service;

    static class TestFilter extends Filter {}

    @BeforeEach
    void setUp() throws Exception {
        cacheManager = new ConcurrentMapCacheManager("taskPages");
        WebClient.Builder builder = mock(WebClient.Builder.class);
        when(builder.build()).thenReturn(webClient);

        service = new CMTaskService(builder, cacheManager);

        var field = CMTaskService.class.getDeclaredField("registryService");
        field.setAccessible(true);
        field.set(service, registryService);
    }

    @Test
    void shouldCachePageResults() {
        mockTaskResponse(new TaskDTO[]{task(1L), task(2L)});

        StepVerifier.create(service.getDBItems(1, 15, filter()))
                .expectNextCount(1)
                .verifyComplete();

        clearInvocations(webClient);
        StepVerifier.create(service.getDBItems(1, 15, filter()))
                .expectNextCount(1)
                .verifyComplete();

        verify(webClient, never()).get();
    }

    @Test
    void shouldCacheDifferentPages() {
        TaskDTO[] tasks = new TaskDTO[]{
                task(1L), task(2L), task(3L), task(4L), task(5L),
                task(6L), task(7L), task(8L), task(9L), task(10L),
                task(11L), task(12L), task(13L), task(14L), task(15L),
                task(16L), task(17L), task(18L)
        };
        mockTaskResponse(tasks);

        service.getDBItems(1, 15, filter()).block();
        service.getDBItems(2, 15, filter()).block();

        var cache = cacheManager.getCache("taskPages");
        assertThat(cache.get("page-1-size-15-sort-id-order-ASC")).isNotNull();
        assertThat(cache.get("page-2-size-15-sort-id-order-ASC")).isNotNull();
    }

    @Test
    void shouldInvalidateCacheOnCreate() {
        mockTaskResponse(new TaskDTO[]{task(1L), task(2L)});

        service.getDBItems(1, 15, filter()).block();
        var cache = cacheManager.getCache("taskPages");
        assertThat(cache.get("page-1-size-15-sort-id-order-ASC")).isNotNull();

        cache.clear();

        assertThat(cache.get("page-1-size-15-sort-id-order-ASC")).isNull();
    }
    private void mockTaskResponse(TaskDTO[] tasks) {
        NodeDTO node = new NodeDTO();
        node.setAddress("node1:8080");

        when(registryService.getActiveAndManagedNodes()).thenReturn(List.of(node));
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TaskDTO[].class)).thenReturn(Mono.just(tasks));
    }

    private Filter filter() {
        Filter f = new TestFilter();
        f.setSortBy("id");
        f.setSortOrder(SortOrder.ASC);
        return f;
    }

    private TaskDTO task(Long id) {
        TaskDTO t = new TaskDTO();
        t.setId(id);
        t.setName("Task " + id);
        t.setClientId(1L);
        t.setSource("/source/" + id);
        t.setEnabled(true);
        t.setInterval(TaskFrequency.DAILY);
        return t;
    }
}