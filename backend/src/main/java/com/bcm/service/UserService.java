package com.bcm.service;

import com.bcm.domain.Group;
import com.bcm.domain.User;
import com.bcm.domain.UserGroupRelation;
import com.bcm.repository.GroupRepository;
import com.bcm.repository.UserGroupRelationRepository;
import com.bcm.repository.UserRepository;
import com.bcm.service.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository users;
    private final GroupRepository groups;
    private final UserGroupRelationRepository userGroups;

    public UserService(UserRepository users,
                       GroupRepository groups,
                       UserGroupRelationRepository userGroups) {
        this.users = users;
        this.groups = groups;
        this.userGroups = userGroups;
    }

    public User create(User u) {
        if (u.getId() == null) u.setId(UUID.randomUUID());
        return users.save(u);
    }

    @Transactional(readOnly = true)
    public User get(UUID id) {
        return users.findById(id).orElseThrow(() ->
                new NotFoundException("User %s not found".formatted(id)));
    }

    @Transactional(readOnly = true)
    public List<User> listAll() { return users.findAll(); }

    public User update(UUID id, User patch) {
        User u = get(id);
        if (patch.getName() != null) u.setName(patch.getName());
        if (patch.getPasswordHash() != null) u.setPasswordHash(patch.getPasswordHash());
        u.setEnabled(patch.isEnabled());
        return users.save(u);
    }

    public void delete(UUID id) {
        // delete relations first (FK)
        userGroups.deleteByUserIdAndGroupId(id, id); // no-op for safety
        users.deleteById(id);
    }

    public void addToGroup(UUID userId, UUID groupId) {
        get(userId);
        Group g = groups.findById(groupId).orElseThrow(() ->
                new NotFoundException("Group %s not found".formatted(groupId)));
        if (!userGroups.existsByUserIdAndGroupId(userId, groupId)) {
            userGroups.save(new UserGroupRelation(userId, groupId));
        }
    }

    public void removeFromGroup(UUID userId, UUID groupId) {
        userGroups.deleteByUserIdAndGroupId(userId, groupId);
    }
}
