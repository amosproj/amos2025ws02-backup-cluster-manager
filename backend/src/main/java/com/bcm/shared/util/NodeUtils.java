package com.bcm.shared.util;

import com.bcm.shared.model.api.NodeDTO;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class NodeUtils {

    private NodeUtils() {}

    public static List<String> addresses(Collection<NodeDTO> nodes) {
        if (nodes == null) return List.of();
        return nodes.stream()
                .filter(Objects::nonNull)
                .map(NodeDTO::getAddress)
                //TODO: remove hardcoded cluster manager address & find a better way to handle it
                .filter(address -> !"cluster-manager:8080".equals(address))
                .collect(Collectors.toList());
    }

    public static String buildNodeUrl(String nodeAddress, String endpoint) {
        Objects.requireNonNull(nodeAddress, "Node address cannot be null");
        Objects.requireNonNull(endpoint, "Endpoint cannot be null");
        return "http://" + nodeAddress + endpoint;
    }
}