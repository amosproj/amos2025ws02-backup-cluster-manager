package com.bcm.mybatis.mapper;

import com.bcm.mybatis.model.Task;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface TaskMapper {
    Task findById(UUID id);
    List<Task> findByClient(UUID clientId);
    int insert(Task t);
    int update(Task t);
    int delete(UUID id);
}
