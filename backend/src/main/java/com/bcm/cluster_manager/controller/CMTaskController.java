package com.bcm.cluster_manager.controller;


import com.bcm.shared.config.permissions.Permission;
import com.bcm.cluster_manager.model.api.BigTaskDTO;
import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.model.database.Task;
import com.bcm.cluster_manager.service.CMTaskService;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller for cluster manager tasks: list and create tasks across nodes.
 */
@RestController()
@RequestMapping("/api/v1/cm")
public class CMTaskController {

    @Autowired
    CMTaskService CMTaskService;

    /**
     * Returns a paginated list of tasks across all nodes.
     *
     * @param pagination pagination and filter parameters
     * @return paginated response of task DTOs
     */
    @PreAuthorize(Permission.Require.TASK_READ)
    @GetMapping("/tasks")
    public Mono<PaginationResponse<BigTaskDTO>> getTasks(PaginationRequest pagination) {
        return CMTaskService.getPaginatedItems(pagination);
    }

    /**
     * Returns all tasks as a list (no pagination).
     *
     * @return list of task DTOs
     */
    @GetMapping("/tasks/list")
    public Mono<List<BigTaskDTO>> getTasksList() {
        return CMTaskService.getAllTasksReactive();
    }

    /**
     * Creates a new task on the target node.
     *
     * @param taskDTO task DTO with node and task details
     * @return the created task DTO
     */
    @PreAuthorize(Permission.Require.TASK_CREATE)
    @PostMapping("/task")
    public Mono<BigTaskDTO> createTask(@RequestBody BigTaskDTO taskDTO) {
        return CMTaskService.addTaskReactive(taskDTO);
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
