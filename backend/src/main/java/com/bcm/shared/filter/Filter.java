package com.bcm.shared.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Filter {
    String search = null;
    String sortBy = null;
    SortOrder sortOrder = SortOrder.ASC;
}
