package com.bcm.shared.filter;

import java.util.*;
import java.util.stream.Collectors;

import com.bcm.shared.sort.SortOrder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Filter {
    String active = "false";
    String search = null;
    String sortBy = null;
    SortOrder sortOrder = SortOrder.ASC;
    private Set<String> filters = null;

    // Custom setter to handle comma-separated string from query parameter
    // Converts "filter1,filter2,filter3" into Set of ["filter1", "filter2", "filter3"]
    public void setFilters(Set<String> filters) {
        if (filters == null || filters.isEmpty()) {
            this.filters = null;
            return;
        }

        // If the set contains a single comma-separated string, split it
        this.filters = filters.stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
