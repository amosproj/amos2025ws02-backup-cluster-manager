package com.bcm.cluster_manager.repository;

import com.bcm.cluster_manager.model.database.Task;
import com.bcm.shared.model.database.Backup;
import com.bcm.shared.model.database.Group;
import com.bcm.shared.model.database.User;
import org.apache.ibatis.annotations.*;

import java.time.Instant;
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

    @Mapper
    interface BackupMapper {

        @Select("""
                SELECT
                    id,
                    client_id AS clientId,
                    task_id AS taskId,
                    start_time AS startTime,
                    stop_time AS stopTime,
                    size_bytes AS sizeBytes,
                    state,
                    message,
                    created_at AS createdAt
                FROM backups
                WHERE id = #{id}
                """)
        Backup findById(Long id);

        @Select("""
            SELECT
                id,
                client_id AS clientId,
                task_id AS taskId,
                start_time AS startTime,
                stop_time AS stopTime,
                size_bytes AS sizeBytes,
                state,
                message,
                created_at AS createdAt
            FROM backups
            WHERE client_id = #{clientId}
            ORDER BY start_time DESC
            """)
        List<Backup> findByClient(Long clientId);

        @Select("""
            SELECT
                id,
                client_id AS clientId,
                task_id AS taskId,
                start_time AS startTime,
                stop_time AS stopTime,
                size_bytes AS sizeBytes,
                state,
                message,
                created_at AS createdAt
            FROM backups
            WHERE task_id = #{taskId}
            ORDER BY start_time DESC
            """)
        List<Backup> findByTask(Long taskId);

        @Select("""
            SELECT
                id,
                client_id AS clientId,
                task_id AS taskId,
                start_time AS startTime,
                stop_time AS stopTime,
                size_bytes AS sizeBytes,
                state,
                message,
                created_at AS createdAt
            FROM backups
            WHERE state = #{state}
            ORDER BY start_time DESC
            """)
        List<Backup> findByState(String state);

        //get All
        @Select("""
            SELECT
                id,
                client_id AS clientId,
                task_id AS taskId,
                start_time AS startTime,
                stop_time AS stopTime,
                size_bytes AS sizeBytes,
                state,
                message,
                created_at AS createdAt
            FROM backups
            ORDER BY start_time DESC
            """)
        List<Backup> findAll();

        @Select("""
            SELECT
                id,
                client_id AS clientId,
                task_id AS taskId,
                start_time AS startTime,
                stop_time AS stopTime,
                size_bytes AS sizeBytes,
                state,
                message,
                created_at AS createdAt
            FROM backups
            WHERE start_time BETWEEN #{from} AND #{to}
            ORDER BY start_time DESC
            """)
        List<Backup> findBetween(Instant from, Instant to);

        @Insert("""
            INSERT INTO backups (
                client_id,
                task_id,
                start_time,
                stop_time,
                size_bytes,
                state,
                message,
                created_at
            ) VALUES (
                #{clientId},
                #{taskId},
                #{startTime},
                #{stopTime},
                #{sizeBytes},
                #{state}::backup_state,
                #{message},
                #{createdAt}
            )
            """)
        @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
        int insert(Backup b);

        @Update("""
            UPDATE backups SET
                client_id = #{clientId},
                task_id = #{taskId},
                start_time = #{startTime},
                stop_time = #{stopTime},
                size_bytes = #{sizeBytes},
                state = #{state}::backup_state,
                message = #{message},
            WHERE id = #{id}
            """)
        int update(Backup b);

        @Delete("""
                DELETE FROM backups
                WHERE id = #{id}
                """)
        int delete(Long id);
    }

    @Mapper
    interface GroupMapper {

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

    @Mapper
    interface UserMapper {

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
}
