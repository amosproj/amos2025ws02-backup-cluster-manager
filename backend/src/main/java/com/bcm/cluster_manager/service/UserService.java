package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.UserDTO;
import com.bcm.shared.pagination.PaginationProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService extends PaginationProvider<UserDTO> {
    public List<UserDTO> exampleUsers;

    public UserService(){
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
    protected long getTotalItemsCount() {
        // Should make a call to the DB to get the actual count
        return exampleUsers.size();
    }

    @Override
    protected List<UserDTO> getDBItems(long page, long itemsPerPage) {
        // Should make a call to the DB to get the actual items
        return exampleUsers.stream()
                .skip((page - 1) * itemsPerPage)
                .limit(itemsPerPage)
                .toList();
    }
}
