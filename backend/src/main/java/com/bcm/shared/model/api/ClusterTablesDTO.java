package com.bcm.shared.model.api;


import java.util.Collection;

public class ClusterTablesDTO {
    private Collection<NodeDTO> active;
    private Collection<NodeDTO> inactive;
    public ClusterTablesDTO() {}
    public ClusterTablesDTO(Collection<NodeDTO> active, Collection<NodeDTO> inactive) {
        this.active = active;
        this.inactive = inactive;
    }
    public Collection<NodeDTO> getActive() { return active; }
    public void setActive(Collection<NodeDTO> active) { this.active = active; }
    public Collection<NodeDTO> getInactive() { return inactive; }
    public void setInactive(Collection<NodeDTO> inactive) { this.inactive = inactive; }
}
