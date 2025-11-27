package com.bcm.cluster_manager.controller;


import com.bcm.cluster_manager.model.api.TaskDTO;
import com.bcm.cluster_manager.model.database.Task;
import com.bcm.cluster_manager.service.TaskService;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/v1")
public class TaskController {

    @Autowired
    TaskService taskService;

    @GetMapping("/tasks")
    public PaginationResponse<TaskDTO> getTasks(PaginationRequest pagination) {
        return taskService.getPaginatedItems(pagination);
    }

    @PostMapping("/task")
    public TaskDTO createTask(@RequestBody TaskDTO taskDTO) {
        return taskService.addTask(toEntity(taskDTO));
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
