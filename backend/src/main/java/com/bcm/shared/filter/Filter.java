package com.bcm.shared.filter;

import java.util.Set;

import com.bcm.shared.model.api.NodeStatus;
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
    Set<NodeStatus> filters = null;
}
