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

    private final ConcurrentHashMap<Long, NodeDTO> active = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, NodeDTO> inactive = new ConcurrentHashMap<>();

    public void register(RegisterRequest req) {
        NodeDTO newNode = new NodeDTO( NodeIdGenerator.nextId(), req.getAddress(), req.getAddress(), NodeStatus.ACTIVE, req.getMode(), req.getMode().equals(NodeMode.CLUSTER_MANAGER) /* sets isManaged flag to true for CM self-registration, false per default for all new nodes*/,
                LocalDateTime.now());
        markActive(newNode);
    }

    public void markActive(NodeDTO inputNode) {
        NodeDTO node = getOrUseInput(inputNode);
        node.setStatus(NodeStatus.ACTIVE);
        inactive.remove(node.getId());
        active.put(node.getId(), node);
    }

    public void markInactive(NodeDTO inputNode) {
        NodeDTO node = getOrUseInput(inputNode);
        node.setStatus(NodeStatus.INACTIVE);
        active.remove(node.getId());
        inactive.put(node.getId(), node);
    }

    public void removeNode(Long id) {
        active.remove(id);
        inactive.remove(id);
    }

    public void updateIsManaged(NodeDTO inputNode) {
        NodeDTO node = getOrUseInput(inputNode);
        node.setIsManaged(inputNode.getIsManaged());
        if (node.getStatus() == NodeStatus.ACTIVE) {
            active.put(node.getId(), node);
        } else {
            inactive.put(node.getId(), node);
        }
    }

    private NodeDTO getOrUseInput(@NotNull NodeDTO inputNode) {
        NodeDTO node = active.get(inputNode.getId());
        if (node == null) node = inactive.get(inputNode.getId());
        if (node == null) return inputNode;

        return node;
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
        ConcurrentHashMap<Long, NodeDTO> merged = new ConcurrentHashMap<>(active);
        inactive.forEach(merged::putIfAbsent);
        return merged.values();
    }
}