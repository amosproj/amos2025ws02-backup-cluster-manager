package com.bcm.shared.repository;

import com.bcm.shared.model.database.UserGroupRelation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface UserGroupRelationRepository {
    List<UserGroupRelation> findByUser(UUID userId);
    List<UserGroupRelation> findByGroup(UUID groupId);
    int insert(UserGroupRelation rel);
    int delete(UUID userId, UUID groupId);
    boolean exists(UUID userId, UUID groupId);
}
