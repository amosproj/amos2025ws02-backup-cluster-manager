package com.bcm.shared.repository;

import com.bcm.shared.model.database.Group;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface GroupMapper extends ReactiveCrudRepository<Group, Long> {

    @Query("""
        SELECT id, name, enabled, created_at, updated_at
        FROM "groups"
        ORDER BY id
    """)
    Flux<Group> findAllGroups();
}