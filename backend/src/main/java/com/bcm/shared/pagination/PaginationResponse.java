package com.bcm.shared.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PaginationResponse<T> {
    private List<T> items;
    private long currentPage;
    private long totalPages;
    private long totalItems;
}
