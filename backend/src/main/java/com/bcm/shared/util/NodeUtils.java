package com.bcm.shared.util;

import com.bcm.shared.model.api.NodeDTO;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility methods for working with node addresses and URLs.
 * Provides helpers for extracting addresses from node collections and constructing HTTP URLs.
 */
public final class NodeUtils {

    private NodeUtils() {}

    /**
     * Extracts the address strings from a collection of nodes.
     * Filters out null nodes and returns a list of their addresses.
     *
     * @param nodes collection of nodes, may be null
     * @return list of node addresses, empty list if input is null or empty
     */
    @NonNull
    public static List<String> addresses(Collection<NodeDTO> nodes) {
        if (nodes == null) return List.of();
        return nodes.stream()
                .filter(Objects::nonNull)
                .map(NodeDTO::getAddress)
                .collect(Collectors.toList());
    }

    /**
     * Ensures that the given address has exactly one {@code http://} prefix.
     * If the input already starts with {@code http://} or {@code https://},
     * it is returned unchanged. Otherwise, {@code http://} is prepended.
     *
     * @param address raw address or URL
     * @return address that is safe to use as a base HTTP URL
     */
    @NonNull
    public static String ensureHttpPrefix(String address) {
        Objects.requireNonNull(address, "Address cannot be null");
        if (address.isEmpty()) return address;

        String lower = address.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return address;
        }

        return "http://" + address;
    }

    /**
     * Constructs a complete HTTP URL for a node endpoint.
     * Ensures the node address has an {@code http://} prefix (if needed) and appends the endpoint.
     * If the endpoint does not start with a forward slash, one is prepended.
     *
     * @param nodeAddress the node's address (e.g., "localhost:8080" or "http://node1:8080")
     * @param endpoint the API endpoint path (e.g., "/api/v1/ping" or "api/v1/ping")
     * @return complete HTTP URL (e.g., "http://localhost:8080/api/v1/ping")
     * @throws NullPointerException if nodeAddress or endpoint is null
     */
    @NonNull
    public static String buildNodeUrl(String nodeAddress, String endpoint) {
        Objects.requireNonNull(nodeAddress, "Node address cannot be null");
        Objects.requireNonNull(endpoint, "Endpoint cannot be null");
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        return ensureHttpPrefix(nodeAddress) + endpoint;
    }
}
