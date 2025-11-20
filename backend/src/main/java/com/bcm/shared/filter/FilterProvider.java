package com.bcm.shared.filter;

import com.bcm.shared.model.api.NodeDTO;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FilterProvider extends Filter {

	/**
	 * Filters a list of NodeDTOs based on the filter criteria.
	 * Supports filtering by 'enabled' and 'search' fields.
	 *
	 * @param nodes List of NodeDTOs to filter
	 * @param filter Filter criteria (can be null)
	 * @return Filtered list of NodeDTOs
	 */
	public static List<NodeDTO> filterNodes(List<NodeDTO> nodes, Filter filter) {
		if (filter == null) return nodes;

		return nodes.stream()
			.filter(node -> {
				// Filter by enabled if set
				if (filter.getActive() != null) {
                    if (filter.getActive()) {
                        // If active filter is true, only include ACTIVE nodes
                        if (!Objects.equals(node.getStatus(), "Active")) {
                            return false;
                        }
                    } else {
                        // If active filter is false, only include INACTIVE nodes
                        if (Objects.equals(node.getStatus(), "Inactive")) {
                            return false;
                        }
                    }
                }
				// Filter by search (matches id, name, address, status)
				if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
					String search = filter.getSearch().toLowerCase();
					boolean matches = (node.getId() != null && node.getId().toString().toLowerCase().contains(search))
						|| (node.getName() != null && node.getName().toLowerCase().contains(search))
						|| (node.getAddress() != null && node.getAddress().toLowerCase().contains(search))
						|| (node.getStatus() != null && node.getStatus().toLowerCase().contains(search));
					if (!matches) {
						return false;
					}
				}
				return true;
			})
			.collect(Collectors.toList());
	}
}