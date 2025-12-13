package com.bcm.cluster_manager.model.api;

import com.bcm.shared.model.api.ClientDTO;
import com.bcm.shared.model.api.NodeDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
public class BigClientDTO extends ClientDTO {
    private NodeDTO nodeDTO;


}
