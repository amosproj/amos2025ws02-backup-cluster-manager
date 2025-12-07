package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.NodeDTO;
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

        registry.register(addr);

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
        registry.register(addr);

        registry.markInactive(addr);

        assertThat(registry.getActiveNodes()).isEmpty();
        assertThat(registry.getInactiveNodes()).hasSize(1);

        NodeDTO node = registry.getInactiveNodes().iterator().next();
    assertThat(node.getStatus()).isEqualTo(com.bcm.shared.model.api.NodeStatus.INACTIVE);
    }

    @Test
    void markActive_createsNodeIfMissing() {
        String addr = "newNode";

        registry.markActive(addr);

        assertThat(registry.getActiveNodes()).hasSize(1);
        assertThat(registry.getInactiveNodes()).isEmpty();

        NodeDTO node = registry.getActiveNodes().iterator().next();
        assertThat(node.getAddress()).isEqualTo(addr);
    assertThat(node.getStatus()).isEqualTo(com.bcm.shared.model.api.NodeStatus.ACTIVE);
    }

    @Test
    void markInactive_createsNodeIfMissing() {
        String addr = "ghostNode";

        registry.markInactive(addr);

        assertThat(registry.getInactiveNodes()).hasSize(1);
        assertThat(registry.getActiveNodes()).isEmpty();

        NodeDTO node = registry.getInactiveNodes().iterator().next();
    assertThat(node.getStatus()).isEqualTo(com.bcm.shared.model.api.NodeStatus.INACTIVE);
    }

    @Test
    void getAllNodes_returnsMergedActiveAndInactive() {
        registry.register("A");
        registry.markInactive("B");

        Collection<NodeDTO> all = registry.getAllNodes();

        assertThat(all).hasSize(2);
        assertThat(all.stream().map(NodeDTO::getAddress))
                .containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void register_overwritesInactiveEntry() {
        registry.markInactive("nodeX");

        registry.register("nodeX");

        assertThat(registry.getActiveNodes()).hasSize(1);
        assertThat(registry.getInactiveNodes()).isEmpty();

        NodeDTO node = registry.getActiveNodes().iterator().next();
    assertThat(node.getStatus()).isEqualTo(com.bcm.shared.model.api.NodeStatus.ACTIVE);
    }

    @Test
    void markActive_movesFromInactiveToActive() {
        registry.markInactive("node1");

        registry.markActive("node1");

        assertThat(registry.getInactiveNodes()).isEmpty();
        assertThat(registry.getActiveNodes()).hasSize(1);

        NodeDTO node = registry.getActiveNodes().iterator().next();
        assertThat(node.getStatus()).isEqualTo(com.bcm.shared.model.api.NodeStatus.ACTIVE);
    }
}