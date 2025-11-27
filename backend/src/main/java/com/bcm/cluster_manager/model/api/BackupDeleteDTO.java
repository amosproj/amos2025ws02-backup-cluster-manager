package com.bcm.cluster_manager.model.api;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BackupDeleteDTO {
    private Long backupId;
    private List<String> nodeAddresses;

    public BackupDeleteDTO(Long backupId, List<String> nodeAddresses) {
        this.backupId = backupId;
        this.nodeAddresses = nodeAddresses;
    }
}
