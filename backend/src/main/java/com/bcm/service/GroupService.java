package com.bcm.service;

import com.bcm.domain.Group;
import com.bcm.repository.GroupRepository;
import com.bcm.service.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groups;

    public GroupService(GroupRepository groups) {
        this.groups = groups;
    }

    public Group create(Group g) {
        if (g.getId() == null) g.setId(UUID.randomUUID());
        return groups.save(g);
    }

    @Transactional(readOnly = true)
    public Group get(UUID id) {
        return groups.findById(id).orElseThrow(() ->
                new NotFoundException("Group %s not found".formatted(id)));
    }

    @Transactional(readOnly = true)
    public List<Group> listAll() { return groups.findAll(); }

    public Group update(UUID id, Group patch) {
        Group g = get(id);
        if (patch.getName() != null) g.setName(patch.getName());
        g.setEnabled(patch.isEnabled());
        return groups.save(g);
    }

    public void delete(UUID id) { groups.deleteById(id); }
}
