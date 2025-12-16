package com.bcm.cluster_manager.model.api;

import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.NodeDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BigBackupDTO extends BackupDTO {
    private NodeDTO nodeDTO;
}
