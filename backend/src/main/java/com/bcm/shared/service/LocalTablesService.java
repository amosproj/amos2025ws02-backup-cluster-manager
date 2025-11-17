package com.bcm.shared.service;

import com.bcm.shared.model.api.NodeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocalTablesService {
    private static final Logger log = LoggerFactory.getLogger(LocalTablesService.class);

    private final ConcurrentHashMap<String, NodeDTO> active = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NodeDTO> inactive = new ConcurrentHashMap<>();

    public void replaceAll(Collection<NodeDTO> newActive, Collection<NodeDTO> newInactive) {
        active.clear();
        inactive.clear();
        if (newActive != null) newActive.forEach(n -> {active.put(n.getAddress(), n); log.info("Active node " + n.getAddress());});
        if (newInactive != null) newInactive.forEach(n -> {inactive.put(n.getAddress(), n); log.info("Inactive node " + n.getAddress());});
    }

    public Collection<NodeDTO> getActive() { return active.values(); }
    public Collection<NodeDTO> getInactive() { return inactive.values(); }
}