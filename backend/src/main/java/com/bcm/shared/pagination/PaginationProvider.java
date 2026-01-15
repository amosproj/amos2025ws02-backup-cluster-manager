package com.bcm.shared.pagination;

import com.bcm.shared.pagination.filter.Filter;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PaginationProvider<T> {
    Mono<List<T>> getDBItems(long page, long itemsPerPage, Filter filter);
     Mono<Long> getTotalItemsCount(Filter filter);

    default Mono<PaginationResponse<T>> getPaginatedItems(PaginationRequest paginationRequest) {
        long page = paginationRequest.getPage();
        long itemsPerPage = paginationRequest.getItemsPerPage();

        Filter filter = paginationRequest;

        return getTotalItemsCount(filter)
                .flatMap(totalItems -> {
                    long totalPages = (long) Math.ceil((double) totalItems / itemsPerPage);

                    long safePage = 0;
                    if (safePage > totalPages) {
                        safePage = totalPages;
                    } else {
                        safePage = page;
                    }

                    long finalSafePage = safePage;
                    return getDBItems(safePage, itemsPerPage, filter)
                            .map(items -> new PaginationResponse<>(items, finalSafePage, totalPages, totalItems));
                });
    }
}