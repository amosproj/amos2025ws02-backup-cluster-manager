package com.bcm.cluster_manager.repository;

import com.bcm.shared.model.database.Group;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GroupMapper {

    @Select("""
                SELECT id, name, enabled, created_at AS createdAt, updated_at AS updatedAt
                FROM "groups"
                WHERE id = #{id}
                """)
    Group findById(Long id);

    @Select("""
                SELECT id, name, enabled, created_at AS createdAt, updated_at AS updatedAt
                FROM "groups"
                ORDER BY id
                """)
    List<Group> findAll();

    @Insert("""
                INSERT INTO "groups" (name, enabled, created_at, updated_at)
                VALUES (#{name}, #{enabled}, #{createdAt}, #{updatedAt})
                """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Group g);

    @Update("""
                UPDATE "groups"
                SET name = #{name},
                    enabled = #{enabled},
                    updated_at = #{updatedAt}
                WHERE id = #{id}
                """)
    int update(Group g);

    @Delete("""
               DELETE FROM "groups"
               WHERE id = #{id}
               """)
    int delete(Long id);
}