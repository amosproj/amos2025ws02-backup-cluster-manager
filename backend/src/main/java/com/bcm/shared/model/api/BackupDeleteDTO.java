package com.bcm.shared.model.api;

import java.util.List;

public class BackupDeleteDTO {
    private Long backupId;
    private List<String> nodeAddresses;

    public BackupDeleteDTO(Long backupId, List<String> nodeAddresses) {
        this.backupId = backupId;
        this.nodeAddresses = nodeAddresses;
    }

    public Long getBackupId() {
        return backupId;
    }
    public void setBackupId(Long backupId) {
        this.backupId = backupId;
    }
    public List<String> getNodeAddresses() {
        return nodeAddresses;
    }
    public void setNodeAddresses(List<String> nodeAddresses) {
        this.nodeAddresses = nodeAddresses;
    }

}
