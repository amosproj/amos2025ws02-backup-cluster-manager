package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.BigClientDTO;
import com.bcm.shared.model.api.ClientDTO;
import com.bcm.shared.model.api.NodeDTO;
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
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CMClientServiceTest {

    @Mock private RegistryService registryService;
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec uriSpec;
    @Mock private WebClient.RequestHeadersSpec headersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    private CacheManager cacheManager;
    private CMClientService service;

    static class TestFilter extends Filter {}

    @BeforeEach
    void setUp() throws Exception {
        cacheManager = new ConcurrentMapCacheManager("clientPages");
        WebClient.Builder builder = mock(WebClient.Builder.class);
        when(builder.build()).thenReturn(webClient);

        service = new CMClientService(builder, cacheManager);

        // Inject registryService using reflection
        var field = CMClientService.class.getDeclaredField("registryService");
        field.setAccessible(true);
        field.set(service, registryService);
    }

    @Test
    void shouldCachePageResults() {
        mockClientResponse(List.of(client(1L), client(2L)));

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
        List<BigClientDTO> clients = List.of(
                client(1L), client(2L), client(3L), client(4L), client(5L),
                client(6L), client(7L), client(8L), client(9L), client(10L),
                client(11L), client(12L), client(13L), client(14L), client(15L),
                client(16L), client(17L), client(18L)
        );
        mockClientResponse(clients);

        service.getDBItems(1, 15, filter()).block();
        service.getDBItems(2, 15, filter()).block();

        var cache = cacheManager.getCache("clientPages");
        assertThat(cache.get("page-1-size-15-sort-id-order-ASC")).isNotNull();
        assertThat(cache.get("page-2-size-15-sort-id-order-ASC")).isNotNull();
    }

    private void mockClientResponse(List<BigClientDTO> clients) {
        NodeDTO node = new NodeDTO();
        node.setAddress("node1:8080");

        when(registryService.getActiveAndManagedNodes()).thenReturn(List.of(node));
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(BigClientDTO.class)).thenReturn(Flux.fromIterable(clients));
    }

    private Filter filter() {
        Filter f = new TestFilter();
        f.setSortBy("id");
        f.setSortOrder(SortOrder.ASC);
        return f;
    }

    private BigClientDTO client(Long id) {
        BigClientDTO c = new BigClientDTO();
        c.setId(id);
        c.setNameOrIp("Client " + id);
        c.setEnabled(true);
        return c;
    }

}