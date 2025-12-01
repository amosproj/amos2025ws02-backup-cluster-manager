package com.bcm.cluster_manager.repository;

import com.bcm.cluster_manager.model.database.Backup;
import org.apache.ibatis.annotations.*;

import java.time.Instant;
import java.util.List;

@Mapper
public interface BackupMapper {

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
                message = #{message}
            WHERE id = #{id}
            """)
    int update(Backup b);

    @Delete("""
                DELETE FROM backups
                WHERE id = #{id}
                """)
    int delete(Long id);
}