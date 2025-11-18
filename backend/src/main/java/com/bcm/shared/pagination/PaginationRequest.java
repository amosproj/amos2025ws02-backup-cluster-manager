package com.bcm.shared.pagination;

public class PaginationRequest {
    // default values
    private long page = 1;
    private long itemsPerPage = 20;

    // Spring setters and getters to populate the fields
    public long getPage() {
        return this.page;
    }

    public long getItemsPerPage() {
        return this.itemsPerPage;
    }

    public void setPage(long page) {
        this.page = (page <= 0) ? 1 : page;
    }

    public void setItemsPerPage(long itemsPerPage) {
        this.itemsPerPage = (itemsPerPage <= 0) ? 20 : itemsPerPage;
    }


}
