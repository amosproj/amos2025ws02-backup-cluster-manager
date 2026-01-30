package com.bcm.shared.controller;


import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.model.database.Task;
import com.bcm.shared.service.TaskService;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller for backup node tasks: list and create tasks.
 */
@RestController()
@RequestMapping("/api/v1/bn")
public class TaskController {

    @Autowired
    TaskService taskService;

    /**
     * Returns all tasks as DTOs.
     *
     * @param pagination pagination parameters (may be unused)
     * @return list of task DTOs
     */
    @GetMapping("/tasks")
    public Mono<List<TaskDTO>> getTasks(PaginationRequest pagination) {
        return taskService.getAllTasks();
    }

    /**
     * Creates a new task.
     *
     * @param taskDTO task data; clientId must not be null
     * @return the created task DTO
     */
    @PostMapping("/task")
    public Mono<TaskDTO> createTask(@RequestBody TaskDTO taskDTO) {
        Task task = toEntity(taskDTO);
        if (task.getClientId() == null) return Mono.error(new IllegalArgumentException("clientId must not be null"));
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
