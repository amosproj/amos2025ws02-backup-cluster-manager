package com.bcm.shared.service;

import com.bcm.shared.model.database.Task;
import com.bcm.shared.repository.TaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    final private TaskMapper taskMapper;

    public TaskService(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Transactional
    public Task getTaskById(Long taskId) {
        return taskMapper.findById(taskId);
    }

    @Transactional
    public List<Task> getTasksOfClient(Long clientId) {
        return taskMapper.findByClient(clientId);
    }

    @Transactional
    public Task addTask(Task task) {
        taskMapper.insert(task);
        return taskMapper.findById(task.getId());
    }

    @Transactional
    public Task editTask(Task task) {
        taskMapper.update(task);
        return taskMapper.findById(task.getId());
    }

    @Transactional
    public boolean deleteTask(Long taskId) {
        return taskMapper.delete(taskId) == 1;
    }
}
