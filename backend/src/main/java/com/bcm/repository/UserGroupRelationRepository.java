package com.bcm.repository;

import com.bcm.domain.UserGroupRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserGroupRelationRepository
        extends JpaRepository<UserGroupRelation, UserGroupRelation.Key> {

    List<UserGroupRelation> findByUserId(UUID userId);
    List<UserGroupRelation> findByGroupId(UUID groupId);
    boolean existsByUserIdAndGroupId(UUID userId, UUID groupId);
    long deleteByUserIdAndGroupId(UUID userId, UUID groupId);
}
