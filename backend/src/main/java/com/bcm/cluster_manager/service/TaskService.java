package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.database.Task;
import com.bcm.cluster_manager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    final private TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId);
    }

    @Transactional
    public List<Task> getTasksOfClient(Long clientId) {
        return taskRepository.findByClient(clientId);
    }

    @Transactional
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional
    public Task addTask(Task task) {
        taskRepository.insert(task);
        return taskRepository.findById(task.getId());
    }

    @Transactional
    public Task editTask(Task task) {
        taskRepository.update(task);
        return taskRepository.findById(task.getId());
    }

    @Transactional
    public boolean deleteTask(Long taskId) {
        return taskRepository.delete(taskId) == 1;
    }
}
