package com.bcm.shared.model.api;


import com.bcm.shared.model.database.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SyncDTO {
    private List<User> cmUsers;
}
