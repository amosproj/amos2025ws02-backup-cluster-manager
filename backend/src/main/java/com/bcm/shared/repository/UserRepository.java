package com.bcm.shared.repository;

import com.bcm.shared.model.database.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface UserRepository {
    User findById(UUID id);
    User findByName(String name);
    List<User> findAll();
    int insert(User user);
    int update(User user);
    int delete(UUID id);
}
