package com.bcm.cluster_manager.service.sort;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.sort.SortProvider;

import java.util.Comparator;
import java.util.Map;

public class BackupComparators {

    /**
     * Registry of all available comparators for BackupDTO
     */
    public static final Map<String, Comparator<BackupDTO>> COMPARATORS = Map.of(
            "id", SortProvider.comparing(BackupDTO::getId),
            "name", SortProvider.comparingIgnoreCase(BackupDTO::getName),
            "state", SortProvider.comparing(BackupDTO::getState),
            "clientid", SortProvider.comparing(BackupDTO::getClientId),
            "taskid", SortProvider.comparing(BackupDTO::getTaskId),
            "sizebytes", SortProvider.comparing(BackupDTO::getSizeBytes),
            "createdat", SortProvider.comparing(BackupDTO::getCreatedAt)
    );
}
