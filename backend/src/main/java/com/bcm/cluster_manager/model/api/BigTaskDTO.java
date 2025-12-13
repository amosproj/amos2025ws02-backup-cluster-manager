package com.bcm.cluster_manager.model.api;

import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.TaskDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class BigTaskDTO extends TaskDTO {
    private NodeDTO nodeDTO;
}
