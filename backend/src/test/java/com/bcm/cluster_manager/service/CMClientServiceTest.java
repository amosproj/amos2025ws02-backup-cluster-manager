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
    void shouldReturnEmptyListWhenNoNodes() {
        when(registryService.getActiveAndManagedNodes()).thenReturn(List.of());

        StepVerifier.create(service.getAllClients())
                .expectNext(List.of())
                .verifyComplete();
    }

    @Test
    void shouldHandleNodeFailureGracefully() {
        NodeDTO node1 = new NodeDTO();
        node1.setAddress("node1:8080");
        NodeDTO node2 = new NodeDTO();
        node2.setAddress("node2:8080");

        when(registryService.getActiveAndManagedNodes()).thenReturn(List.of(node1, node2));
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(BigClientDTO.class))
                .thenReturn(Flux.error(new RuntimeException("Connection failed")))
                .thenReturn(Flux.just(client(1L)));

        StepVerifier.create(service.getAllClients())
                .assertNext(result -> assertThat(result).hasSize(1))
                .verifyComplete();
    }

    @Test
    void shouldFetchAndMergeClientsFromMultipleNodes() {
        NodeDTO node1 = new NodeDTO();
        node1.setAddress("node1:8080");
        node1.setName("Node 1");

        NodeDTO node2 = new NodeDTO();
        node2.setAddress("node2:8080");
        node2.setName("Node 2");

        // Clients from node1
        BigClientDTO client1 = new BigClientDTO();
        client1.setId(1L);
        client1.setNameOrIp("Client from Node1");
        client1.setEnabled(true);

        // Clients from node2
        BigClientDTO client2 = new BigClientDTO();
        client2.setId(2L);
        client2.setNameOrIp("Client from Node2");
        client2.setEnabled(false);

        when(registryService.getActiveAndManagedNodes()).thenReturn(List.of(node1, node2));
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        // First call returns clients from node1, second call returns clients from node2
        when(responseSpec.bodyToFlux(BigClientDTO.class))
                .thenReturn(Flux.just(client1))
                .thenReturn(Flux.just(client2));

        StepVerifier.create(service.getAllClients())
                .assertNext(result -> {
                    assertThat(result).hasSize(2);

                    // Verify client 1 has node1
                    BigClientDTO mappedClient1 = result.stream()
                            .filter(c -> c.getId().equals(1L))
                            .findFirst()
                            .orElseThrow();
                    assertThat(mappedClient1.getNodeDTO()).isEqualTo(node1);
                    assertThat(mappedClient1.getNameOrIp()).isEqualTo("Client from Node1");

                    // Verify client 2 has node2
                    BigClientDTO mappedClient2 = result.stream()
                            .filter(c -> c.getId().equals(2L))
                            .findFirst()
                            .orElseThrow();
                    assertThat(mappedClient2.getNodeDTO()).isEqualTo(node2);
                    assertThat(mappedClient2.getNameOrIp()).isEqualTo("Client from Node2");
                })
                .verifyComplete();

        verify(uriSpec).uri("http://node1:8080/api/v1/bn/clients");
        verify(uriSpec).uri("http://node2:8080/api/v1/bn/clients");
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