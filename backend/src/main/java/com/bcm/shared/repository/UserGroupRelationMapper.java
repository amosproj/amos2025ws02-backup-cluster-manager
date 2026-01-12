package com.bcm.shared.repository;

import com.bcm.shared.model.database.UserGroupRelation;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserGroupRelationMapper extends ReactiveCrudRepository<UserGroupRelation, Long> {

    @Query("""
        SELECT user_id, group_id, added_at
        FROM user_group_relations
        WHERE user_id = :userId
    """)
    Flux<UserGroupRelation> findByUser(Long userId);

    @Query("""
        SELECT user_id, group_id, added_at
        FROM user_group_relations
        WHERE group_id = :groupId
    """)
    Flux<UserGroupRelation> findByGroup(Long groupId);

    @Modifying
    @Query("""
        INSERT INTO user_group_relations (user_id, group_id, added_at)
        VALUES (:#{#rel.userId}, :#{#rel.groupId}, :#{#rel.addedAt})
    """)
    Mono<Integer> insert(UserGroupRelation rel);

    @Modifying
    @Query("""
        DELETE FROM user_group_relations
        WHERE user_id = :userId AND group_id = :groupId
    """)
    Mono<Integer> delete(Long userId, Long groupId);

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM user_group_relations
            WHERE user_id = :userId AND group_id = :groupId
        )
    """)
    Mono<Boolean> exists(Long userId, Long groupId);
}
