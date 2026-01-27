package com.bcm.shared.repository;

import com.bcm.shared.model.database.Task;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

public interface TaskMapper extends ReactiveCrudRepository<Task, Long> {

    @Query("""
    SELECT id, name, client_id, source, enabled, created_at, updated_at, "interval"
    FROM tasks
    WHERE client_id = :clientId
    """)
    Flux<Task> findByClient(Long clientId);


    @Query("""
    SELECT id, name, client_id, source, enabled, created_at, updated_at, "interval"
    FROM tasks
    """)
    Flux<Task> findAllTasks();

    @Query("""
    INSERT INTO tasks (name, client_id, source, enabled, "interval")
    VALUES (:name, :clientId, :source, :enabled, :interval::frequency_enum)
    RETURNING id
    """)
    Mono<Long> insertAndReturnId(String name, Long clientId, String source, boolean enabled, String interval);

    @Modifying
    @Query("""
    UPDATE tasks
    SET name = :name,
        client_id = :clientId,
        source = :source,
        enabled = :enabled,
        updated_at = :updatedAt,
        "interval" = :interval::frequency_enum
    WHERE id = :id
    """)
    Mono<Integer> update(Long id, String name, Long clientId, String source, boolean enabled, Instant updatedAt, String interval);

}
