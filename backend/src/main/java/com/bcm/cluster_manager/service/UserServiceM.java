package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.UserDTO;
import com.bcm.shared.pagination.PaginationProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceM implements PaginationProvider<UserDTO> {
    public List<UserDTO> exampleUsers;

    // Define searchable columns - only changeable in backend
    private static final List<String> SEARCHABLE_COLUMNS = List.of("name", "id");

    public UserServiceM(){
        int numBackups = 1000;
        List<UserDTO> list = new ArrayList<>();
        for (int i = 1; i <= numBackups; i++) {
            list.add(new UserDTO(
                    (long) i,
                    "user" + i,
                    true,
                    LocalDateTime.now().minusDays(i),
                    LocalDateTime.now().minusDays(i+1)
            ));
        }
        exampleUsers = list;
    }

    @Override
    public long getTotalItemsCount() {
        return exampleUsers.size();
    }

    @Override
    public long getTotalItemsCount(String search) {
        if (search == null || search.trim().isEmpty()) {
            return getTotalItemsCount();
        }
        return filterBySearch(exampleUsers, search).size();
    }

    @Override
    public List<UserDTO> getDBItems(long page, long itemsPerPage) {
        return exampleUsers.stream()
                .skip((page - 1) * itemsPerPage)
                .limit(itemsPerPage)
                .toList();
    }

    @Override
    public List<UserDTO> getDBItems(long page, long itemsPerPage, String search) {
        if (search == null || search.trim().isEmpty()) {
            return getDBItems(page, itemsPerPage);
        }

        List<UserDTO> filtered = filterBySearch(exampleUsers, search);
        return filtered.stream()
                .skip((page - 1) * itemsPerPage)
                .limit(itemsPerPage)
                .toList();
    }


    // Filter users by searching in defined columns

    private List<UserDTO> filterBySearch(List<UserDTO> users, String search) {
        String searchLower = search.toLowerCase().trim();

        return users.stream()
                .filter(user -> {
                    // Search in name column
                    if (user.getName() != null &&
                        user.getName().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                    // Search in id column (convert to string)
                    if (user.getId() != null &&
                        user.getId().toString().contains(searchLower)) {
                        return true;
                    }
                    return false;
                })
                .toList();
    }
}
