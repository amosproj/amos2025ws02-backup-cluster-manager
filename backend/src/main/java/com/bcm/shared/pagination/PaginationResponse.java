package com.bcm.shared.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Response for a paginated request: items, current page, total pages, total count.
 *
 * @param <T> type of item
 */
@Getter
@AllArgsConstructor
public class PaginationResponse<T> {
    private List<T> items;
    private long currentPage;
    private long totalPages;
    private long totalItems;
}
