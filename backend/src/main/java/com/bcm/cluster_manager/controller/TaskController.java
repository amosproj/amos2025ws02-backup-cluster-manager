package com.bcm.cluster_manager.controller;


import com.bcm.cluster_manager.model.api.TaskDTO;
import com.bcm.cluster_manager.model.database.Task;
import com.bcm.cluster_manager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/v1")
public class TaskController {

    @Autowired
    TaskService taskService;

    @GetMapping("/tasks")
    public List<TaskDTO> getTasks() {
        return taskService.getAllTasks().stream()
                .map(this::toDto)
                .toList();
    }

    @PostMapping("/task")
    public TaskDTO createTask(TaskDTO taskDTO) {
         Task task = taskService.addTask(toEntity(taskDTO));
         return toDto(task);
    }

    private TaskDTO toDto(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setClientId(task.getClientId());
        dto.setSource(task.getSource());
        dto.setEnabled(task.isEnabled());
        dto.setInterval(task.getInterval());
        return dto;
    }

    private Task toEntity(TaskDTO taskDTO) {
        Task task = new Task();
        task.setId(taskDTO.getId());
        task.setName(taskDTO.getName());
        task.setClientId(taskDTO.getClientId());
        task.setSource(taskDTO.getSource());
        task.setEnabled(taskDTO.isEnabled());
        task.setInterval(taskDTO.getInterval());
        return task;
    }


}
