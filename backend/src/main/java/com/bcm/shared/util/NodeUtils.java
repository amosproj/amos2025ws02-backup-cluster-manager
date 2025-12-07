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
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}