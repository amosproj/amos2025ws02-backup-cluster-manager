package com.bcm.shared.pagination;

import com.bcm.shared.pagination.filter.Filter;

import java.util.List;

public interface PaginationProvider<T> {
    abstract List<T> getDBItems(long page, long itemsPerPage, Filter filter);
    abstract long getTotalItemsCount(Filter filter);

    public default PaginationResponse<T> getPaginatedItems(PaginationRequest paginationRequest){
        long page = paginationRequest.getPage();
        long itemsPerPage = paginationRequest.getItemsPerPage();

        Filter filter = paginationRequest;

        long totalItems = getTotalItemsCount(filter);

        List<T> items;
        long totalPages = (long) Math.ceil((double) totalItems / itemsPerPage);

        // Check if requested page exceeds total pages
        if (page > totalPages && totalPages != 0) {
            page = totalPages; // Set to last available page
        }
        items = getDBItems(page, itemsPerPage, filter);

        return new PaginationResponse<T>(items, page, totalPages, totalItems);
    }
}