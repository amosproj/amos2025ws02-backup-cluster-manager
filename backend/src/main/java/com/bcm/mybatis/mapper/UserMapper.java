package com.bcm.mybatis.mapper;

import com.bcm.mybatis.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface UserMapper {
    User findById(UUID id);
    User findByName(String name);
    List<User> findAll();
    int insert(User user);
    int update(User user);
    int delete(UUID id);
}
