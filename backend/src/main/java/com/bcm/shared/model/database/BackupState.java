package com.bcm.shared.model.database;

public enum BackupState {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELED
}
