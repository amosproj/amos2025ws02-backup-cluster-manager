package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.NodeStatus;
import com.bcm.shared.model.api.RegisterRequest;
import com.bcm.shared.service.NodeIdGenerator;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistryService {

    private final ConcurrentHashMap<String, NodeDTO> active = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NodeDTO> inactive = new ConcurrentHashMap<>();

    public void register(RegisterRequest req) {
        NodeDTO newNode = new NodeDTO( NodeIdGenerator.nextId(), req.getAddress(), req.getAddress(), NodeStatus.ACTIVE, req.getMode(), req.getMode().equals(NodeMode.CLUSTER_MANAGER) /* sets isManaged flag to true for CM self-registration, false per default for all new nodes*/,
                LocalDateTime.now());
        markActive(newNode);
    }

    public void markActive(NodeDTO node) {
        NodeDTO info = getOrUseInput(node);
        info.setStatus(NodeStatus.ACTIVE);
        inactive.remove(node.getAddress());
        active.put(node.getAddress(), info);
    }

    public void markInactive(NodeDTO node) {
        NodeDTO info = getOrUseInput(node);
        info.setStatus(NodeStatus.INACTIVE);
        active.remove(node.getAddress());
        inactive.put(node.getAddress(), info);
    }

    private NodeDTO getOrUseInput(@NotNull NodeDTO node) {
        NodeDTO info = active.get(node.getAddress());
        if (info == null) info = inactive.get(node.getAddress());
        if (info == null) return node;

        return info;
    }

    public Collection<NodeDTO> getActiveAndManagedNodes() {
        return active.values().stream().filter(NodeDTO::getIsManaged).toList();
    }

    public Collection<NodeDTO> getActiveNodes() {
        return active.values();
    }
    public Collection<NodeDTO> getInactiveNodes() {
        return inactive.values();
    }

    public Collection<NodeDTO> getAllNodes() {
        ConcurrentHashMap<String, NodeDTO> merged = new ConcurrentHashMap<>(active);
        inactive.forEach(merged::putIfAbsent);
        return merged.values();
    }
}