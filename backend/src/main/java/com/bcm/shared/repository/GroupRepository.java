package com.bcm.shared.repository;

import com.bcm.shared.model.database.Group;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface GroupRepository {
    Group findById(UUID id);
    List<Group> findAll();
    int insert(Group g);
    int update(Group g);
    int delete(UUID id);
}
