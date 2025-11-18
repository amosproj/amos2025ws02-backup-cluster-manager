package com.bcm.shared.pagination;

import java.util.List;

public interface PaginationProvider<T> {
    abstract List<T> getDBItems(long page, long itemsPerPage);
    abstract long getTotalItemsCount();

    public default List<T> getDBItems(long page, long itemsPerPage, String search) {
        return getDBItems(page, itemsPerPage);
    }

    public default long getTotalItemsCount(String search) {
        return getTotalItemsCount();
    }

    public default PaginationResponse<T> getPaginatedItems(long page, long itemsPerPage){
        return getPaginatedItems(page, itemsPerPage, "");
    }

    public default PaginationResponse<T> getPaginatedItems(long page, long itemsPerPage, String search){
        long totalItems = getTotalItemsCount(search);
        List<T> items;
        long totalPages = (long) Math.ceil((double) totalItems / itemsPerPage);

        // Check if requested page exceeds total pages
        if (page > totalPages && totalPages != 0) {
            page = totalPages; // Set to last available page
        }
        items = getDBItems(page, itemsPerPage, search);

        return new PaginationResponse<T>(items, page, totalPages);
    }

}
