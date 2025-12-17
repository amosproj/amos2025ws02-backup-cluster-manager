package com.bcm.shared.service;

import com.bcm.shared.config.permissions.Role;
import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.model.database.Group;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupMapper groupMapper;

    public GroupService(@Qualifier("groupMapperBN") GroupMapper groupMapper) {
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

    /**
     * Gets the rank of a group based on its role mapping.
     *
     * @param group the group to get the rank for
     * @return the rank value, or 0 if the group doesn't map to a valid role
     */
    private int getGroupRank(Group group) {
        try {
            Role role = Role.valueOf(group.getName().toUpperCase().replace(' ', '_'));
            return role.getRank();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Retrieves all groups filtered by requester's rank.
     * Only returns groups with a lower rank than the requester.
     *
     * @param requesterRank the rank of the requesting user
     * @return list of groups with lower rank
     */
    @Transactional
    public List<Group> getAllGroupsWithRankCheck(int requesterRank) {
        return groupMapper.findAll().stream()
                .filter(group -> group.isEnabled())
                .filter(group -> getGroupRank(group) < requesterRank)
                .collect(Collectors.toList());
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
