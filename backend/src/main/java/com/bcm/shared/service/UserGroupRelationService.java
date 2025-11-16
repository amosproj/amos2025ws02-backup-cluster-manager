package com.bcm.shared.service;

import com.bcm.shared.model.database.UserGroupRelation;
import com.bcm.shared.repository.UserGroupRelationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserGroupRelationService {

    private final UserGroupRelationMapper userGroupRelationMapper;

    public UserGroupRelationService(UserGroupRelationMapper userGroupRelationMapper) {
        this.userGroupRelationMapper = userGroupRelationMapper;
    }

    public List<UserGroupRelation> getGroupsForUser(Long userId) {
        return userGroupRelationMapper.findByUser(userId);
    }

    public List<UserGroupRelation> getUsersForGroup(Long groupId) {
        return userGroupRelationMapper.findByGroup(groupId);
    }

    public boolean addUserToGroup(Long userId, Long groupId) {
        UserGroupRelation userGroupRelation = new UserGroupRelation();
        userGroupRelation.setUserId(userId);
        userGroupRelation.setGroupId(groupId);
        return userGroupRelationMapper.insert(userGroupRelation) == 1;
    }

    public boolean removeUserFromGroup(Long userId, Long groupId) {
        return userGroupRelationMapper.delete(userId, groupId) == 1;
    }
}
