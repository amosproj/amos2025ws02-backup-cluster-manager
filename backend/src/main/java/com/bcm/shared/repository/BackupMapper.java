package com.bcm.shared.repository;

import com.bcm.shared.model.database.Backup;
import com.bcm.shared.model.database.BackupState;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;


public interface BackupMapper extends ReactiveCrudRepository<Backup,Long> {


    Flux<Backup> findByClientIdOrderByStartTimeDesc(Long clientId);

    Flux<Backup> findByTaskIdOrderByStartTimeDesc(Long taskId);

    Flux<Backup> findByStateOrderByStartTimeDesc(BackupState state);

    Flux<Backup> findByStartTimeBetweenOrderByStartTimeDesc(Instant from, Instant to);

    @Query("""
        INSERT INTO backups (client_id, task_id, start_time, size_bytes, state, created_at, message, stop_time) 
        VALUES (:#{#backup.clientId}, :#{#backup.taskId}, :#{#backup.startTime}, 
                :#{#backup.sizeBytes}, :#{#backup.state.name()}::backup_state, 
                :#{#backup.createdAt}, :#{#backup.message}, :#{#backup.stopTime})
        RETURNING *
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