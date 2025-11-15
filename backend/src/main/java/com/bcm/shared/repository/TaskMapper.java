package com.bcm.shared.repository;

import com.bcm.shared.model.database.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper {

    @Select("""
            SELECT id, name, client_id AS clientId, source, enabled, created_at AS createdAt, updated_at AS updatedAt
            FROM tasks WHERE id = #{id}
            """)
    Task findById(Long id);

    @Select("""
            SELECT id, name, client_id AS clientId, source, enabled, created_at AS createdAt, updated_at AS updatedAt
            FROM tasks WHERE client_id = #{clientId}
            """)
    List<Task> findByClient(Long clientId);

    @Insert("""
            INSERT INTO tasks (name, client_id, source, enabled, created_at, updated_at)
            VALUES (#{name}, #{clientId}, #{source}, #{enabled}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Task t);

    @Update("""
            UPDATE tasks
            SET name = #{name},
                client_id = #{clientId},
                source = #{source},
                enabled = #{enabled},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int update(Task t);

    @Delete("""
            DELETE FROM tasks
            WHERE id = #{id}
            """)
    int delete(Long id);
}
