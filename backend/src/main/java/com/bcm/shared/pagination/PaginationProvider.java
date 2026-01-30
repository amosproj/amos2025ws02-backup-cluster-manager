package com.bcm.shared.pagination;

import com.bcm.shared.pagination.filter.Filter;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Provides paginated access to a collection of items with filtering and sorting.
 *
 * @param <T> type of item (e.g. DTO)
 */
public interface PaginationProvider<T> {
    /**
     * Returns a page of items after filtering and sorting.
     *
     * @param page         page number (1-based)
     * @param itemsPerPage page size
     * @param filter       filter, search, and sort parameters
     * @return list of items for the page
     */
    Mono<List<T>> getDBItems(long page, long itemsPerPage, Filter filter);

    /**
     * Returns the total count of items matching the filter.
     *
     * @param filter filter and search parameters
     * @return total count
     */
    Mono<Long> getTotalItemsCount(Filter filter);

    /**
     * Returns a paginated response (items, current page, total pages, total count).
     *
     * @param paginationRequest page, size, filter, search, sort
     * @return paginated response
     */
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