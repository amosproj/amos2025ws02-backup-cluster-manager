package com.bcm.shared.service;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.repository.GroupMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupService {

    private final GroupMapper groupMapper;

    public GroupService(GroupMapper groupMapper) {
        this.groupMapper = groupMapper;
    }

    @Transactional
    public Group getGroupById(Long id) {
        return groupMapper.findById(id);
    }

    @Transactional
    public List<Group> getAllGroups() {
        return groupMapper.findAll();
    }

    @Transactional
    public Group addGroup(Group group) {
        groupMapper.insert(group);
        return groupMapper.findById(group.getId());
    }

    @Transactional
    public Group editGroup(Group group) {
        groupMapper.update(group);
        return groupMapper.findById(group.getId());
    }

    @Transactional
    public boolean deleteGroup(Long id) {
        return groupMapper.delete(id) == 1;
    }
}
