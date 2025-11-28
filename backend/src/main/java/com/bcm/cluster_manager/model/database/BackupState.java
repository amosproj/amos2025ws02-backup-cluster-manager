package com.bcm.cluster_manager.model.database;

public enum BackupState {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELED
}
