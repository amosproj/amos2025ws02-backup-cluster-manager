package com.bcm.shared.repository;

import com.bcm.shared.model.database.Backup;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;


public interface BackupMapper extends ReactiveCrudRepository<Backup,Long> {


    @Query("""
    SELECT id, client_id, task_id, start_time, stop_time, size_bytes, state, message, created_at
    FROM backups
    WHERE client_id = :clientId
    ORDER BY start_time DESC
    """)
    Flux<Backup> findByClient(Long clientId);

    @Query("""
    SELECT id, client_id, task_id, start_time, stop_time, size_bytes, state, message, created_at
    FROM backups
    WHERE task_id = :taskId
    ORDER BY start_time DESC
    """)
    Flux<Backup> findByTask(Long taskId);

    @Query("""
    SELECT id, client_id, task_id, start_time, stop_time, size_bytes, state, message, created_at
    FROM backups
    WHERE state = CAST(:state AS backup_state)
    ORDER BY start_time DESC
    """)
    Flux<Backup> findByState(String state);


    @Query("""
    SELECT id, client_id, task_id, start_time, stop_time, size_bytes, state, message, created_at
    FROM backups
    WHERE start_time BETWEEN :from AND :to
    ORDER BY start_time DESC
    """)
    Flux<Backup> findBetween(Instant from, Instant to);


    @Query("""
    INSERT INTO backups (client_id, task_id, start_time, stop_time, size_bytes, state, message, created_at)
    VALUES (:#{#backup.clientId}, :#{#backup.taskId}, :#{#backup.startTime}, :#{#backup.stopTime}, 
            :#{#backup.sizeBytes}, :#{#backup.state.name()}::backup_state, :#{#backup.message}, :#{#backup.createdAt})
    """)
    Mono<Backup> insert(Backup backup);


    @Modifying
    @Query("""
    UPDATE backups SET client_id = :#{#backup.clientId}, task_id = :#{#backup.taskId}, 
           start_time = :#{#backup.startTime}, stop_time = :#{#backup.stopTime}, 
           size_bytes = :#{#backup.sizeBytes}, state = :#{#backup.state.name()}::backup_state,
           message = :#{#backup.message}
    WHERE id = :#{#backup.id}
    RETURNING *
    """)
    Mono<Integer> update(Backup b);

}