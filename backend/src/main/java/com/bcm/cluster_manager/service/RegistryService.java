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

    public void markActive(NodeDTO inputNode) {
        NodeDTO node = getOrUseInput(inputNode);
        node.setStatus(NodeStatus.ACTIVE);
        inactive.remove(node.getAddress());
        active.put(node.getAddress(), node);
    }

    public void markInactive(NodeDTO inputNode) {
        NodeDTO node = getOrUseInput(inputNode);
        node.setStatus(NodeStatus.INACTIVE);
        active.remove(node.getAddress());
        inactive.put(node.getAddress(), node);
    }

    public void removeNode(Long id) {
        if (id == null) {
            return;
        }
        NodeDTO node = active.values().stream().filter(n -> n.getId().equals(id)).findFirst().orElse(null);

        if (node == null) node = inactive.values().stream().filter(n -> n.getId().equals(id)).findFirst().orElse(null);

        if( node != null && node.getMode().equals(NodeMode.CLUSTER_MANAGER)){
            throw new IllegalArgumentException("Cannot remove cluster manager.");
        }
        if(node != null) {
            String address = node.getAddress();
            active.remove(address);
            inactive.remove(address);
        }
    }

    public void updateIsManaged(NodeDTO inputNode) {
        NodeDTO node = getOrUseInput(inputNode);
        if(node.getMode().equals(NodeMode.CLUSTER_MANAGER)){
            throw new IllegalArgumentException("Cannot change isManaged flag of Cluster Manager node.");
        }
        node.setIsManaged(inputNode.getIsManaged());
        if (node.getStatus() == NodeStatus.ACTIVE) {
            active.put(node.getAddress(), node);
        } else {
            inactive.put(node.getAddress(), node);
        }
    }

    private NodeDTO getOrUseInput(@NotNull NodeDTO inputNode) {
        NodeDTO node = active.get(inputNode.getAddress());
        if (node == null) node = inactive.get(inputNode.getAddress());
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
        ConcurrentHashMap<String, NodeDTO> merged = new ConcurrentHashMap<>(active);
        inactive.forEach(merged::putIfAbsent);
        return merged.values();
    }
}