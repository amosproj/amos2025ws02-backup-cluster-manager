package com.bcm.cluster_manager.repository;

import com.bcm.shared.model.database.UserGroupRelation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserGroupRelationMapper {

    @Select("""
                SELECT user_id AS userId, group_id AS groupId, added_at AS addedAt
                FROM user_group_relations WHERE user_id = #{userId}
                """)
    List<UserGroupRelation> findByUser(@Param("userId") Long userId);

    @Select("""
                SELECT user_id AS userId, group_id AS groupId, added_at AS addedAt
                FROM user_group_relations WHERE group_id = #{groupId}
                """)
    List<UserGroupRelation> findByGroup(@Param("groupId") Long groupId);

    @Insert("""
                INSERT INTO user_group_relations (user_id, group_id, added_at)
                VALUES (#{userId}, #{groupId}, #{addedAt})
                """)
    int insert(UserGroupRelation rel);

    @Delete("""
                DELETE FROM user_group_relations
                WHERE user_id = #{userId} AND group_id = #{groupId}
                """)
    int delete(@Param("userId") Long userId, @Param("groupId") Long groupId);

    @Select("""
                SELECT EXISTS(SELECT 1 FROM user_group_relations
                WHERE user_id = #{userId} AND group_id = #{groupId})
                """)
    boolean exists(@Param("userId") Long userId, @Param("groupId") Long groupId);
}
