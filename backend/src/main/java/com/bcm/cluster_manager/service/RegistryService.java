package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeStatus;
import com.bcm.shared.service.NodeIdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistryService {

    private final ConcurrentHashMap<String, NodeDTO> active = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NodeDTO> inactive = new ConcurrentHashMap<>();

    public void register(String address) {
        NodeDTO info = new NodeDTO( NodeIdGenerator.nextId(), address, address, NodeStatus.ACTIVE, LocalDateTime.now());
        inactive.remove(address);
        active.put(address, info);
    }

    public void markActive(String address) {
        NodeDTO info = getOrCreate(address);
        info.setStatus(NodeStatus.ACTIVE);
        inactive.remove(address);
        active.put(address, info);
    }

    public void markInactive(String address) {
        NodeDTO info = getOrCreate(address);
        info.setStatus(NodeStatus.INACTIVE);
        active.remove(address);
        inactive.put(address, info);
    }

    private NodeDTO getOrCreate(String address) {
        NodeDTO info = active.get(address);
        if (info == null) info = inactive.get(address);
        if (info == null) info = new NodeDTO(NodeIdGenerator.nextId(), address, address, NodeStatus.PENDING, LocalDateTime.now());
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