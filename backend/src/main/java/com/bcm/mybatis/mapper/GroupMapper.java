package com.bcm.mybatis.mapper;

import com.bcm.mybatis.model.Group;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface GroupMapper {
    Group findById(UUID id);
    List<Group> findAll();
    int insert(Group g);
    int update(Group g);
    int delete(UUID id);
}
