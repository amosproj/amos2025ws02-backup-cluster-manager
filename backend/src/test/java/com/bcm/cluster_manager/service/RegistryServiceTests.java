package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.NodeStatus;
import com.bcm.shared.model.api.RegisterRequest;
import com.bcm.shared.service.NodeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class RegistryServiceTests {

    private RegistryService registry;

    @BeforeEach
    void setup() {
        registry = new RegistryService();
    }

    @Test
    void register_addsNodeToActiveAndRemovesFromInactive() {
        String addr = "10.0.0.1:9000";
        RegisterRequest req = new RegisterRequest(addr, NodeMode.NODE, false);
        registry.register(req);

        Collection<NodeDTO> active = registry.getActiveNodes();
        Collection<NodeDTO> inactive = registry.getInactiveNodes();

        assertThat(active).hasSize(1);
        assertThat(inactive).isEmpty();

        NodeDTO node = active.iterator().next();
        assertThat(node.getAddress()).isEqualTo(addr);
    assertThat(node.getStatus()).isEqualTo(com.bcm.shared.model.api.NodeStatus.ACTIVE);
    }

    @Test
    void markInactive_movesNodeToInactive() {
        String addr = "nodeA";
        RegisterRequest req = new RegisterRequest(addr, NodeMode.NODE, false);
        registry.register(req);

        registry.markInactive(new NodeDTO(NodeIdGenerator.nextId(), addr, addr, com.bcm.shared.model.api.NodeStatus.ACTIVE, NodeMode.NODE, false, null));

        assertThat(registry.getActiveNodes()).isEmpty();
        assertThat(registry.getInactiveNodes()).hasSize(1);

        NodeDTO node = registry.getInactiveNodes().iterator().next();
    assertThat(node.getStatus()).isEqualTo(com.bcm.shared.model.api.NodeStatus.INACTIVE);
    }

    @Test
    void markActive_createsNodeIfMissing() {
        String addr = "newNode";

        registry.markActive(new NodeDTO(NodeIdGenerator.nextId(), addr, addr, com.bcm.shared.model.api.NodeStatus.PENDING, NodeMode.NODE, false, null));

        assertThat(registry.getActiveNodes()).hasSize(1);
        assertThat(registry.getInactiveNodes()).isEmpty();

        NodeDTO node = registry.getActiveNodes().iterator().next();
        assertThat(node.getAddress()).isEqualTo(addr);
    assertThat(node.getStatus()).isEqualTo(com.bcm.shared.model.api.NodeStatus.ACTIVE);
    }

    @Test
    void markInactive_createsNodeIfMissing() {
        String addr = "ghostNode";

        registry.markInactive(new NodeDTO(NodeIdGenerator.nextId(), addr, addr, com.bcm.shared.model.api.NodeStatus.PENDING, NodeMode.NODE, false, null));

        assertThat(registry.getInactiveNodes()).hasSize(1);
        assertThat(registry.getActiveNodes()).isEmpty();

        NodeDTO node = registry.getInactiveNodes().iterator().next();
    assertThat(node.getStatus()).isEqualTo(com.bcm.shared.model.api.NodeStatus.INACTIVE);
    }

    @Test
    void getAllNodes_returnsMergedActiveAndInactive() {
        RegisterRequest req = new RegisterRequest("A", NodeMode.NODE, false);

        registry.register(req);
        registry.markInactive(new NodeDTO(NodeIdGenerator.nextId(), "B", "B", com.bcm.shared.model.api.NodeStatus.PENDING, NodeMode.NODE, false, null));

        Collection<NodeDTO> all = registry.getAllNodes();

        assertThat(all).hasSize(2);
        assertThat(all.stream().map(NodeDTO::getAddress))
                .containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void register_overwritesInactiveEntry() {
        registry.markInactive(new NodeDTO(NodeIdGenerator.nextId(), "nodeX", "nodeX", com.bcm.shared.model.api.NodeStatus.PENDING, NodeMode.NODE, false, null));
        RegisterRequest req = new RegisterRequest("nodeX", NodeMode.NODE, false);

        registry.register(req);

        assertThat(registry.getActiveNodes()).hasSize(1);
        assertThat(registry.getInactiveNodes()).isEmpty();

        NodeDTO node = registry.getActiveNodes().iterator().next();
    assertThat(node.getStatus()).isEqualTo(com.bcm.shared.model.api.NodeStatus.ACTIVE);
    }

    @Test
    void markActive_movesFromInactiveToActive() {
        registry.markInactive(new NodeDTO(NodeIdGenerator.nextId(), "node1", "node1", NodeStatus.ACTIVE, NodeMode.NODE, false, null));

        registry.markActive(new NodeDTO(NodeIdGenerator.nextId(), "node1", "node1", NodeStatus.INACTIVE, NodeMode.NODE, false, null));

        assertThat(registry.getInactiveNodes()).isEmpty();
        assertThat(registry.getActiveNodes()).hasSize(1);

        NodeDTO node = registry.getActiveNodes().iterator().next();
        assertThat(node.getStatus()).isEqualTo(com.bcm.shared.model.api.NodeStatus.ACTIVE);
    }
}