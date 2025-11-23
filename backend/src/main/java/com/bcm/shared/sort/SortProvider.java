package com.bcm.shared.sort;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SortProvider {

    /**
     * Sort a list by a field with configurable order
     * @param items List to sort
     * @param sortBy Field name to sort by
     * @param sortOrder "asc" or "desc"
     * @param comparatorMap Map of field names to comparators
     * @return Sorted list
     */
    public static <T> List<T> sort(
            List<T> items,
            String sortBy,
            String sortOrder,
            Map<String, Comparator<T>> comparatorMap) {
        
        if (sortBy == null || sortBy.isBlank() || comparatorMap == null) {
            return items;
        }

        Comparator<T> comparator = comparatorMap.get(sortBy.toLowerCase());
        if (comparator == null) {
            return items; // Unknown sort field, return unsorted
        }

        // Reverse if descending
        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        return items.stream()
            .sorted(comparator)
            .toList();
    }

    /**
     * Create a comparator for a comparable field (String, Long, LocalDateTime, etc.)
     */
    public static <T, C extends Comparable<C>> Comparator<T> comparing(Function<T, C> extractor) {
        return Comparator.comparing(extractor, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    /**
     * Create a case-insensitive comparator for String fields
     */
    public static <T> Comparator<T> comparingIgnoreCase(Function<T, String> extractor) {
        return Comparator.comparing(extractor, Comparator.nullsLast(String::compareToIgnoreCase));
    }

    /**
     * Sort by multiple fields (for tie-breaking)
     */
    @SafeVarargs
    public static <T> List<T> sortByMultiple(
            List<T> items,
            String sortOrder,
            Comparator<T>... comparators) {
        
        if (comparators == null || comparators.length == 0) {
            return items;
        }

        Comparator<T> combined = comparators[0];
        for (int i = 1; i < comparators.length; i++) {
            combined = combined.thenComparing(comparators[i]);
        }

        if ("desc".equalsIgnoreCase(sortOrder)) {
            combined = combined.reversed();
        }

        return items.stream()
            .sorted(combined)
            .toList();
    }
}