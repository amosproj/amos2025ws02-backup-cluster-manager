package com.bcm.shared.repository;

import com.bcm.shared.model.database.User;
import com.bcm.shared.pagination.sort.SortOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final R2dbcEntityTemplate template;

    public UserRepositoryImpl(@Qualifier("bnTemplate") R2dbcEntityTemplate template) {
        this.template = template;
    }

    private String safeSortColumn(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) return "created_at";
        return switch (sortBy) {
            case "id" -> "id";
            case "name" -> "name";
            case "created_at", "createdAt" -> "created_at";
            case "updated_at", "updatedAt" -> "updated_at";
            case "enabled" -> "enabled";
            default -> "created_at"; // same fallback behavior
        };
    }

    @Override
    public Mono<Long> getTotalCount(String search, Boolean isUserEnabled) {
        String sql = """
      SELECT COUNT(*) AS cnt
      FROM users
      WHERE (:search IS NULL OR :search = '' OR
            name ILIKE '%' || :search || '%' OR CAST(id AS TEXT) ILIKE '%' || :search || '%')
        AND (:enabled IS NULL OR enabled = :enabled)
      """;

        return template.getDatabaseClient().sql(sql)
                .bind("search", search)
                .bind("enabled", isUserEnabled)
                .map((row, meta) -> row.get("cnt", Long.class))
                .one();
    }

    @Override
    public Flux<User> getPaginatedAndFilteredUsers(
            long page, long itemsPerPage,
            String search, String sortBy, SortOrder sortOrder,
            Boolean isUserEnabled
    ) {
        long offset = (page - 1) * itemsPerPage;

        String orderBy = safeSortColumn(sortBy);
        String dir = (sortOrder != null && sortOrder.name().equals("DESC")) ? "DESC" : "ASC";

        String sql = """
              SELECT id, name, password_hash, enabled, created_at, updated_at
              FROM users
              WHERE (:search IS NULL OR :search = '' OR
                    name ILIKE '%' || :search || '%' OR CAST(id AS TEXT) ILIKE '%' || :search || '%')
                AND (:enabled IS NULL OR enabled = :enabled)
              ORDER BY %s %s
              LIMIT :limit OFFSET :offset
        """.formatted(orderBy, dir);

        return template.getDatabaseClient().sql(sql)
                .bind("search", search)
                .bind("enabled", isUserEnabled)
                .bind("limit", itemsPerPage)
                .bind("offset", offset)
                .map((row, meta) -> {
                    User u = new User();
                    u.setId(row.get("id", Long.class));
                    u.setName(row.get("name", String.class));
                    u.setPasswordHash(row.get("password_hash", String.class));
                    u.setEnabled(Boolean.TRUE.equals(row.get("enabled", Boolean.class)));
                    u.setCreatedAt(row.get("created_at", Instant.class));
                    u.setUpdatedAt(row.get("updated_at", Instant.class));
                    return u;
                })
                .all();
    }
}
