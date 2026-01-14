package com.bcm.shared.repository;

import com.bcm.shared.model.database.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public interface UserMapper extends ReactiveCrudRepository<User,Integer>, UserRepository {

    Mono<User> findByName(String name);

    @Query("""
    SELECT id, name, password_hash AS passwordHash, enabled, created_at AS createdAt, updated_at AS updatedAt
    FROM users
    WHERE name ILIKE CONCAT('%', :name, '%')
  """)
    Flux<User> findByNameSubtext(String name);

    @Query("DELETE FROM users WHERE id = :id")
    Mono<Void> deleteUserById(Long id);

    @Query("SELECT id, name, password_hash AS passwordHash, enabled, created_at AS createdAt, updated_at AS updatedAt FROM users WHERE id = :id")
    Mono<User> findUserById(Long id);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE id = :id)")
    Mono<Boolean> existsUserById(Long id);

    @Query("UPDATE users SET name = :name, enabled = :enabled, updated_at = :updatedAt WHERE id = :id")
    Mono<Void> updateUser(Long id, String name, Boolean enabled, Instant updatedAt);
}
