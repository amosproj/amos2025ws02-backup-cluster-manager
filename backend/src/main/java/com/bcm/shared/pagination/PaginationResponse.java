package com.bcm.shared.pagination;

import java.util.List;

public class PaginationResponse<T> {
    private List<T> items;
    private long currentPage;
    private long totalPages;

    public PaginationResponse(List<T> items, long currentPage, long totalPages) {
        this.items = items;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    public List<T> getItems() {
        return items;
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public long getTotalPages() {
        return totalPages;
    }
}
