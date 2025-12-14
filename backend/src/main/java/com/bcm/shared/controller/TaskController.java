package com.bcm.shared.controller;


import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.model.database.Task;
import com.bcm.shared.service.TaskService;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/bn")
public class TaskController {

    @Autowired
    TaskService taskService;

    @GetMapping("/tasks")
    public List<TaskDTO> getTasks(PaginationRequest pagination) {
        return taskService.getAllTasks();
    }

    @PostMapping("/task")
    public TaskDTO createTask(@RequestBody TaskDTO taskDTO) {
        //System.out.println("Received task create request: {}" + taskDTO);

        Task task = toEntity(taskDTO);
        if (task.getClientId() == null) {
            throw new IllegalArgumentException("clientId must not be null");
        }

        return taskService.createTask(task);

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
