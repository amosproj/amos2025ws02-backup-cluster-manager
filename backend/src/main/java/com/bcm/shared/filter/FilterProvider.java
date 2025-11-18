package com.bcm.shared.filter;

import java.util.List;
import java.util.function.Function;

public class FilterProvider {

    /**
     * Filter list by active status based on a custom status extractor
     */
    public static <T> List<T> filterByActive(List<T> items, Boolean active, Function<T, String> statusExtractor) {
        if (Boolean.TRUE.equals(active)) {
            return items.stream()
                .filter(item -> "Active".equalsIgnoreCase(statusExtractor.apply(item)))
                .toList();
        }
        return items;
    }

    /**
     * Filter list by multiple search fields (more flexible)
     */
    public static <T> List<T> filterBySearchFields(
            List<T> items, 
            String search, 
            List<Function<T, String>> fieldExtractors) {
        
        if (search == null || search.isBlank()) {
            return items;
        }

        final String term = search.trim().toLowerCase();

        return items.stream().filter(item -> {
            // Check if any field contains the search term
            return fieldExtractors.stream()
                .anyMatch(extractor -> {
                    String value = extractor.apply(item);
                    return value != null && value.toLowerCase().matches(term);
                });
        }).toList();
    }
}