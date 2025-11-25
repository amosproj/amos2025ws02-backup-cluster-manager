package com.bcm.cluster_manager.repository;

import com.bcm.shared.model.database.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("""
                SELECT id, name, password_hash AS passwordHash, enabled, created_at AS createdAt, updated_at AS updatedAt
                FROM users WHERE id = #{id}
                """)
    User findById(@Param("id") Long id);

    @Select("""
                SELECT id, name, password_hash AS passwordHash, enabled, created_at AS createdAt, updated_at AS updatedAt FROM users WHERE name = #{name}
                """)
    User findByName(String name);

    @Select("""
                SELECT id, name, password_hash AS passwordHash, enabled, created_at AS createdAt, updated_at AS updatedAt FROM users WHERE name ILIKE CONCAT('%', #{name}, '%')
                """)
    List<User> findByNameSubtext(String name);

    @Select("""
                SELECT id, name, password_hash AS passwordHash, enabled, created_at AS createdAt, updated_at AS updatedAt FROM users
                """)
    List<User> findAll();

    @Insert("""
                INSERT INTO users (name, password_hash, enabled, created_at, updated_at)
                VALUES (#{name}, #{passwordHash}, #{enabled}, #{createdAt}, #{updatedAt})
                """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("""
                <script>
                UPDATE users
                <set>
                    <if test="name != null">name = #{name},</if>
                    <if test="passwordHash != null">password_hash = #{passwordHash},</if>
                    <if test="enabled != null">enabled = #{enabled},</if>
                    updated_at = #{updatedAt}
                </set>
                WHERE id = #{id}
                </script>
                """)
    int update(User user);

    @Delete("""
               DELETE FROM users
               WHERE id = #{id}
               """)
    int delete(Long id);
}
