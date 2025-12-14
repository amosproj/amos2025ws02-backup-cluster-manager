package com.bcm.cluster_manager.service.pagination.shared;

import com.bcm.cluster_manager.model.api.BigBackupDTO;
import com.bcm.shared.pagination.sort.BackupComparators;
import com.bcm.shared.pagination.sort.SortProvider;

import java.util.Comparator;
import java.util.Map;

public class BigBackupComparators extends BackupComparators {


    public static final Map<String, Comparator<BigBackupDTO>> COMPARATORS = Map.of(
            "id", SortProvider.comparing(BigBackupDTO::getId),
            "clientId", SortProvider.comparing(BigBackupDTO::getClientId),
            "taskId", SortProvider.comparing(BigBackupDTO::getTaskId),
            "name", SortProvider.comparing(BigBackupDTO::getName),
            "state", SortProvider.comparing(BigBackupDTO::getState),
            "sizeBytes", SortProvider.comparing(BigBackupDTO::getSizeBytes),
            "startTime", SortProvider.comparing(BigBackupDTO::getStartTime),
            "stopTime", SortProvider.comparing(BigBackupDTO::getStopTime),
            "createdAt", SortProvider.comparing(BigBackupDTO::getCreatedAt)
    );
}

