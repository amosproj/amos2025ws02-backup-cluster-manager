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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistryService {

    private final ConcurrentHashMap<String, NodeDTO> active = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NodeDTO> inactive = new ConcurrentHashMap<>();

    public void register(RegisterRequest req) {
        // Check if node already exists (re-registration after restart)
        NodeDTO existing = active.get(req.getAddress());
        if (existing == null) {
            existing = inactive.get(req.getAddress());
        }

        if (existing != null) {
            // Node is re-registering - update status to ACTIVE
            existing.setStatus(NodeStatus.ACTIVE);
            existing.setMode(req.getMode());
            inactive.remove(req.getAddress());
            active.put(req.getAddress(), existing);
            
        } else {
            // New node registration - same logic as before
            // Node not managed by default
            NodeDTO newNode = new NodeDTO(NodeIdGenerator.nextId(), req.getAddress(), req.getAddress(), NodeStatus.ACTIVE, req.getMode(), false, LocalDateTime.now());
//            inactive.remove(info.getAddress());
//            active.put(info.getAddress(), info);
            markActive(newNode);
        }
    }

    public void markActive(NodeDTO node) {
        NodeDTO info = getOrUseInput(node);
        // Only update status if not in a transitional state, or if re-registering after restart
        if (info.getStatus() == NodeStatus.RESTARTING) {
            // Node is back after restart - update to ACTIVE
            info.setStatus(NodeStatus.ACTIVE);
        } else if (info.getStatus() != NodeStatus.SHUTTING_DOWN) {
            // Normal case - mark as active
            info.setStatus(NodeStatus.ACTIVE);
        }
        // If SHUTTING_DOWN, keep that status until removed or re-registered
        inactive.remove(node.getAddress());
        active.put(node.getAddress(), info);
    }


    public void markInactive(NodeDTO node) {
        NodeDTO info = getOrUseInput(node);
        // Don't override RESTARTING status during heartbeat failures (node will come back)
        // But DO override SHUTTING_DOWN - if heartbeat fails, node is now fully down (INACTIVE)
        if (info.getStatus() == NodeStatus.RESTARTING) {
            // Keep RESTARTING status - node will register when it comes back
        } else if (info.getStatus() == NodeStatus.SHUTTING_DOWN) {
            // Node was shutting down
            info.setStatus(NodeStatus.INACTIVE);
        } else {
            info.setStatus(NodeStatus.INACTIVE);
        }
        active.remove(node.getAddress());
        inactive.put(node.getAddress(), info);
    }

    private NodeDTO getOrUseInput(NodeDTO node) {
        NodeDTO info = active.get(node.getAddress());
        if (info == null) info = inactive.get(node.getAddress());
        // Create new node - initialized as not managed
        if (info == null) info = new NodeDTO(NodeIdGenerator.nextId(), node.getAddress(), node.getAddress(), NodeStatus.PENDING, node.getMode(), false, LocalDateTime.now());
        return info;
    }

    public Collection<NodeDTO> getActiveAndManagedNodes() {
        return active.values().stream().filter(NodeDTO::getIsManaged).toList();
    }

        public void markShuttingDown(String address) {
            NodeDTO info = findByAddress(address).orElse(null);
            if (info != null) {
                info.setStatus(NodeStatus.SHUTTING_DOWN);
            }
        }

        public void markRestarting(String address) {
            NodeDTO info = findByAddress(address).orElse(null);
            if (info != null) {
                info.setStatus(NodeStatus.RESTARTING);
            }
        }

        public Optional<NodeDTO> findByAddress(String address) {
            NodeDTO node = active.get(address);
            if (node == null) {
                node = inactive.get(address);
            }
            return Optional.ofNullable(node);
        }

        public Optional<NodeDTO> findById(Long id) {
            return getAllNodes().stream()
                    .filter(node -> node.getId().equals(id))
                    .findFirst();
        }

        public boolean removeNode(String address) {
            NodeDTO removed = active.remove(address);
            if (removed == null) {
                removed = inactive.remove(address);
            }
            return removed != null;
        }

        public boolean removeNodeById(Long id) {
            Optional<NodeDTO> node = findById(id);
            if (node.isPresent()) {
                return removeNode(node.get().getAddress());
            }
            return false;
        }

        public void updateNodeMode(String address, NodeMode mode) {
            NodeDTO info = findByAddress(address).orElse(null);
            if (info != null) {
                info.setMode(mode);
            }
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

    

    public Collection<NodeDTO> getActiveNodes() { return active.values(); }
    public Collection<NodeDTO> getInactiveNodes() { return inactive.values(); }

    public Collection<NodeDTO> getAllNodes() {
        ConcurrentHashMap<String, NodeDTO> merged = new ConcurrentHashMap<>(active);
        inactive.forEach(merged::putIfAbsent);
        return merged.values();
    }
}