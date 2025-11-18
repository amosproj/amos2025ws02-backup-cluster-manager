package com.bcm.shared.pagination;

import java.util.List;

public abstract class PaginationProvider<T> {
    protected abstract List<T> getDBItems(long page, long itemsPerPage);
    protected abstract long getTotalItemsCount();

    public PaginationResponse<T> getPaginatedItems(long page, long itemsPerPage){
        long totalItems = getTotalItemsCount();
        List<T> items;
        long totalPages = (long) Math.ceil((double) totalItems / itemsPerPage);

        // Check if requested page exceeds total pages
        if (page > totalPages && totalPages != 0) {
            page = totalPages; // Set to last available page
        }
        items = getDBItems(page, itemsPerPage);

        return new PaginationResponse<T>(items, page, totalPages);
    }

}
