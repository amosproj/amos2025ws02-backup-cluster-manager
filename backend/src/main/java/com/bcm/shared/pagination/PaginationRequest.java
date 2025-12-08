package com.bcm.shared.pagination;

import lombok.Getter;
import lombok.Setter;
import com.bcm.shared.pagination.filter.Filter;

@Setter
@Getter
public class PaginationRequest extends Filter {
    // default values
    private long page = 1;
    private long itemsPerPage = 20;

    public void setPage(long page) {
        this.page = (page <= 0) ? 1 : page;
    }

    public void setItemsPerPage(long itemsPerPage) {
        this.itemsPerPage = (itemsPerPage <= 0) ? 20 : itemsPerPage;
    }

}
