package com.bcm.cluster_manager.service.pagination.shared;

import com.bcm.cluster_manager.model.api.BigTaskDTO;
import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.pagination.sort.TaskComparators;

import java.util.Comparator;
import java.util.Map;

public class BigTaskComparators extends TaskComparators {


    public static final Map<String, Comparator<BigTaskDTO>> COMPARATORS = Map.of(
            "id", SortProvider.comparing(BigTaskDTO::getId),
            "name", SortProvider.comparingIgnoreCase(BigTaskDTO::getName),
            "interval", SortProvider.comparing(BigTaskDTO::getInterval),
            "clientid", SortProvider.comparing(BigTaskDTO::getClientId),
            "source", SortProvider.comparing(BigTaskDTO::getSource),
            "enabled", SortProvider.comparing(BigTaskDTO::isEnabled)
    );
}

