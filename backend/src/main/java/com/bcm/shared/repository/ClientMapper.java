package com.bcm.shared.repository;

import com.bcm.cluster_manager.model.api.BigClientDTO;
import com.bcm.shared.model.database.Client;
import org.apache.ibatis.annotations.*;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ClientMapper extends ReactiveCrudRepository<Client, Long> {

    @Query("""
        SELECT id, name_or_ip, enabled, created_at, updated_at
        FROM clients
        WHERE name_or_ip = :nameOrIp
    """)
    Mono<Client> findByNameOrIp(String nameOrIp);

    @Query("""
        SELECT id, name_or_ip, enabled, created_at, updated_at
        FROM clients
    """)
    Flux<Client> findAllClients();
}
