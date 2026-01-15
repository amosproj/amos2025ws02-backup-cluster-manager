package com.bcm.shared.repository;

import com.bcm.shared.model.database.User;
import com.bcm.shared.pagination.sort.SortOrder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<Long> getTotalCount(String search, Boolean isUserEnabled);

    Flux<User> getPaginatedAndFilteredUsers(
            long page, long itemsPerPage,
            String search, String sortBy, SortOrder sortOrder,
            Boolean isUserEnabled
    );

}
