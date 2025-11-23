package com.bcm.cluster_manager.repository;

import com.bcm.cluster_manager.model.database.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskRepository {

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
            INSERT INTO tasks (name, client_id, source, enabled, created_at, updated_at, "interval")
            VALUES (#{name}, #{clientId}, #{source}, #{enabled}, #{createdAt}, #{updatedAt}, #{interval})
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
                "interval" = #{interval}
            WHERE id = #{id}
            """)
    int update(Task t);

    @Delete("""
            DELETE FROM tasks
            WHERE id = #{id}
            """)
    int delete(Long id);
}
