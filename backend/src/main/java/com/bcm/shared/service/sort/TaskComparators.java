package com.bcm.shared.service.sort;

import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.pagination.sort.SortProvider;

import java.util.Comparator;
import java.util.Map;

public class TaskComparators {

    /**
     * Registry of all available comparators for BackupDTO
     */
    public static final Map<String, Comparator<TaskDTO>> COMPARATORS = Map.of(
            "id", SortProvider.comparing(TaskDTO::getId),
            "name", SortProvider.comparingIgnoreCase(TaskDTO::getName),
            "interval", SortProvider.comparing(TaskDTO::getInterval),
            "clientid", SortProvider.comparing(TaskDTO::getClientId),
            "source", SortProvider.comparing(TaskDTO::getSource),
            "enabled", SortProvider.comparing(TaskDTO::isEnabled)
    );
}