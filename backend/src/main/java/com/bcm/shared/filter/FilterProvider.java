package com.bcm.shared.filter;

import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeStatus;
import java.util.List;
import java.util.Objects;
import org.springframework.util.StringUtils;
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
				// Active filter: expect a boolean-like string ("true"/"false")
				boolean activeOnly = filter.getActive().equalsIgnoreCase("true");
				if (activeOnly && node.getStatus() != NodeStatus.ACTIVE) return false;
				if (!activeOnly && node.getStatus() == NodeStatus.ACTIVE) return false; // assuming false means exclude active
				// Search across id, name, address, status enum name
				if (StringUtils.hasText(filter.getSearch())) {
					String search = filter.getSearch().toLowerCase();
					boolean matches = (node.getId() != null && node.getId().toString().toLowerCase().contains(search))
						|| (node.getName() != null && node.getName().toLowerCase().contains(search))
						|| (node.getAddress() != null && node.getAddress().toLowerCase().contains(search))
						|| (node.getStatus() != null && node.getStatus().toJson().contains(search));
					if (!matches) return false;
				}
				return true;
			})
			.collect(Collectors.toList());
	}
}