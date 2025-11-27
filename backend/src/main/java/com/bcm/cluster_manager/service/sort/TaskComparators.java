package com.bcm.cluster_manager.service.sort;

import com.bcm.cluster_manager.model.api.TaskDTO;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.sort.SortProvider;

import java.util.Comparator;
import java.util.Map;
import com.bcm.cluster_manager.model.api.TaskDTO;

import java.util.Comparator;

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