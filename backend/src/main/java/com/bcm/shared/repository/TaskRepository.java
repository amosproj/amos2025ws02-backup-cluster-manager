package com.bcm.shared.repository;

import com.bcm.shared.model.database.Task;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface TaskRepository {
    Task findById(UUID id);
    List<Task> findByClient(UUID clientId);
    int insert(Task t);
    int update(Task t);
    int delete(UUID id);
}
