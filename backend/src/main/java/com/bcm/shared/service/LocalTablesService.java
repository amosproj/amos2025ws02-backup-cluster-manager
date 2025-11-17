package com.bcm.shared.service;

import com.bcm.shared.model.api.NodeDTO;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocalTablesService {
    private final ConcurrentHashMap<String, NodeDTO> active = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NodeDTO> inactive = new ConcurrentHashMap<>();

    public void replaceAll(Collection<NodeDTO> newActive, Collection<NodeDTO> newInactive) {
        active.clear();
        inactive.clear();
        if (newActive != null) newActive.forEach(n -> active.put(n.getAddress(), n));
        if (newInactive != null) newInactive.forEach(n -> inactive.put(n.getAddress(), n));
    }

    public Collection<NodeDTO> getActive() { return active.values(); }
    public Collection<NodeDTO> getInactive() { return inactive.values(); }
}