package com.bcm.shared.repository;

import com.bcm.shared.model.database.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface TaskMapper {

    @Select("""
            SELECT id, name, client_id AS clientId, source, enabled, created_at AS createdAt, updated_at AS updatedAt, "interval"
            FROM tasks WHERE id = #{id}
            """)
    Task findById(Long id);

    @Select("""
            SELECT id, name, client_id AS clientId, source, enabled, created_at AS createdAt, updated_at AS updatedAt, "interval"
            FROM tasks WHERE client_id = #{clientId}
            """)
    List<Task> findByClient(Long clientId);

    @Select("""
            SELECT id, name, client_id AS clientId, source, enabled, created_at AS createdAt, updated_at AS updatedAt, "interval"
            FROM tasks;
            """)
    List<Task> findAll();

    @Insert("""
            INSERT INTO tasks (name, client_id, source, enabled, "interval")
            VALUES (#{name}, #{clientId}, #{source}, #{enabled}, #{interval}::frequency_enum)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Task t);

    @Update("""
            UPDATE tasks
            SET name = #{name},
                client_id = #{clientId},
                source = #{source},
                enabled = #{enabled},
                updated_at = #{updatedAt},
                "interval" = #{interval}::frequency_enum
            WHERE id = #{id}
            """)
    int update(Task t);

    @Delete("""
            DELETE FROM tasks
            WHERE id = #{id}
            """)
    int delete(Long id);
}
