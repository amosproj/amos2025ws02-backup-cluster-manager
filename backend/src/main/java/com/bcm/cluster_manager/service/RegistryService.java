package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.NodeStatus;
import com.bcm.shared.service.NodeIdGenerator;

import ch.qos.logback.core.pattern.parser.Node;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistryService {

    private final ConcurrentHashMap<String, NodeDTO> active = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NodeDTO> inactive = new ConcurrentHashMap<>();

    public void register(String address, NodeMode mode) {
        NodeDTO info = new NodeDTO( NodeIdGenerator.nextId(), address, address, NodeStatus.ACTIVE, mode, LocalDateTime.now());
        inactive.remove(address);
        active.put(address, info);
    }

    public void markActive(String address, NodeMode mode) {
        NodeDTO info = getOrCreate(address, mode);
        info.setStatus(NodeStatus.ACTIVE);
        inactive.remove(address);
        active.put(address, info);
    }

    public void markInactive(String address, NodeMode mode) {
        NodeDTO info = getOrCreate(address, mode);
        info.setStatus(NodeStatus.INACTIVE);
        active.remove(address);
        inactive.put(address, info);
    }

    private NodeDTO getOrCreate(String address,NodeMode mode) {
        NodeDTO info = active.get(address);
        if (info == null) info = inactive.get(address);
        if (info == null) info = new NodeDTO(NodeIdGenerator.nextId(), address, address, NodeStatus.PENDING, mode, LocalDateTime.now());
        return info;
    }

    public Collection<NodeDTO> getActiveNodes() { return active.values(); }
    public Collection<NodeDTO> getInactiveNodes() { return inactive.values(); }

    public Collection<NodeDTO> getAllNodes() {
        ConcurrentHashMap<String, NodeDTO> merged = new ConcurrentHashMap<>(active);
        inactive.forEach(merged::putIfAbsent);
        return merged.values();
    }
}