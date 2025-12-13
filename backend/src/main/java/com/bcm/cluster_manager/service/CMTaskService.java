package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.BigClientDTO;
import com.bcm.cluster_manager.model.api.BigTaskDTO;
import com.bcm.cluster_manager.service.pagination.shared.BigTaskComparators;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.model.database.Task;
import com.bcm.shared.model.database.TaskFrequency;
import com.bcm.shared.pagination.sort.TaskComparators;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.util.NodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;


@Service
public class CMTaskService implements PaginationProvider<BigTaskDTO> {

    private static final Logger logger = LoggerFactory.getLogger(CMTaskService.class);


    @Autowired
    private RegistryService registryService;

    @Autowired
    private RestTemplate restTemplate;


    @Override
    public long getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        List<BigTaskDTO> base = (getAllTasks());
        return applySearch(applyFilters(base, filter), filter).size();

    }

    @Override
    public List<BigTaskDTO> getDBItems(long page, long itemsPerPage, Filter filter) {
        List<BigTaskDTO> allBackups = (getAllTasks());

        List<BigTaskDTO> filtered = applyFilters(allBackups, filter);
        List<BigTaskDTO> searched = applySearch(filtered, filter);
        List<BigTaskDTO> sorted = SortProvider.sort(
                searched,
                filter.getSortBy(),
                filter.getSortOrder() != null ? filter.getSortOrder().toString() : null,
                BigTaskComparators.COMPARATORS
        );
        int fromIndex = (int) Math.max(0, (page - 1) * itemsPerPage);
        int toIndex = Math.min(fromIndex + (int) itemsPerPage, sorted.size());
        if (fromIndex >= toIndex) {
            return new ArrayList<>();
        }
        return sorted.subList(fromIndex, toIndex);
    }

    public List<BigTaskDTO> getAllTasks() {
        Collection<NodeDTO> nodes = registryService.getActiveNodes();
        if (nodes.isEmpty()) return List.of();

        List<CompletableFuture<BigTaskDTO[]>> futures = nodes.stream().map(node  -> CompletableFuture.supplyAsync(() -> {
            try {
                String url = "http://" + node.getAddress() + "/api/v1/bn/tasks";
                ResponseEntity<TaskDTO[]> response = restTemplate.getForEntity(url, TaskDTO[].class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return Arrays.stream(response.getBody())
                            .map(taskDto -> {
                                BigTaskDTO bigTaskDto = new BigTaskDTO();

                                bigTaskDto.setId(taskDto.getId());
                                bigTaskDto.setName(taskDto.getName());
                                bigTaskDto.setEnabled(taskDto.isEnabled());
                                bigTaskDto.setInterval(taskDto.getInterval());
                                bigTaskDto.setSource(taskDto.getSource());

                                BigClientDTO bigClientDto = new BigClientDTO();
                                bigClientDto.setId(taskDto.getClientId());
                                // Don't need to set NameOrIp and Enabled here, as they are not relevant for task pages
                                bigClientDto.setNodeDTO(node);
                                bigTaskDto.setBigClientDTO(bigClientDto);

                                return bigTaskDto;
                            })
                            .toArray(BigTaskDTO[]::new);
                }
            } catch (Exception e) {
                logger.info("Fehler beim Abruf von Tasks von Node " + node.getAddress() + ". Error: " + (e.getMessage()));
            }
            return null;
        })).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<BigTaskDTO> allTasks = new ArrayList<>();
        //Set<Long> seenIds = new HashSet<>();

        for (CompletableFuture<BigTaskDTO[]> future : futures) {
            try {
                BigTaskDTO[] tasks = future.get();
                if (tasks != null) {
                    allTasks.addAll(Arrays.asList(tasks));
                }
            } catch (Exception ignored) {
            }
        }

        return allTasks;
    }

    // Filters by BackupState parsed from filter.getFilters()
    private List<BigTaskDTO> applyFilters(List<BigTaskDTO> tasks, Filter filter) {
        if (filter == null || filter.getFilters() == null || filter.getFilters().isEmpty()) {
            return tasks;
        }

        var requested = filter.getFilters().stream()
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(s -> {
                    try {
                        return TaskFrequency.valueOf(s);
                    } catch (IllegalArgumentException ex) {
                        return null;
                    }
                })
                .filter(state -> state != null)
                .distinct()
                .toList();

        if (requested.isEmpty()) return tasks;

        if (requested.size() == TaskFrequency.values().length) return tasks;

        return tasks.stream()
                .filter(b -> requested.contains(b.isEnabled()))
                .toList();
    }

    // Searches in name, clientId, taskId, state name, and id (if present)
    private List<BigTaskDTO> applySearch(List<BigTaskDTO> backups, Filter filter) {
        if (filter != null && StringUtils.hasText(filter.getSearch())) {
            String searchTerm = filter.getSearch().toLowerCase();
            return backups.stream()
                    .filter(t ->
                            (t.getName() != null && t.getName().toLowerCase().contains(searchTerm)) ||
                                    (t.getClientId() != null && t.getClientId().toString().toLowerCase().contains(searchTerm)) ||
                                    (t.getId() != null && t.getId().toString().toLowerCase().contains(searchTerm)) ||
                                    (t.getSource() != null && t.getSource().toLowerCase().contains(searchTerm))
                    )
                    .toList();
        }
        return backups;
    }

    /*

        CRUD Operations
     */

    @Transactional
    public BigTaskDTO addTask(BigTaskDTO task) {
        if (task == null ||
                task.getBigClientDTO() == null ||
                task.getBigClientDTO().getNodeDTO() == null ||
                task.getBigClientDTO().getNodeDTO().getId() == null) {
            return null;
        }

        Long targetNodeId = task.getBigClientDTO().getNodeDTO().getId();

        Optional<TaskDTO> result = registryService.getActiveNodes().stream()
                .filter(node -> node.getId().equals(targetNodeId))
                .findFirst()
                .map(node -> {
                    try {
                        String url = "http://" + node.getAddress() + "/api/v1/bn/task";
                        ResponseEntity<TaskDTO> response =
                                restTemplate.postForEntity(url, task, TaskDTO.class);

                        if (response.getStatusCode().is2xxSuccessful()) {
                            return response.getBody();
                        }
                    } catch (Exception e) {
                        logger.error("Fehler beim Hinzufügen von Task an Node " + node.getAddress());
                    }
                    return null;
                });
        if (result.isPresent()) {
            BigTaskDTO createdTask = new BigTaskDTO();
            createdTask.setId(result.get().getId());
            createdTask.setName(result.get().getName());
            createdTask.setClientId(result.get().getClientId());
            createdTask.setSource(result.get().getSource());
            createdTask.setEnabled(result.get().isEnabled());
            createdTask.setInterval(result.get().getInterval());
            createdTask.setBigClientDTO(task.getBigClientDTO());
            return createdTask;
        }

        logger.error("Target node for new Task not found or error occurred.");
        return null;
    }


    @Transactional
    public Task editTask(Task task) {
        return null;
    }

    @Transactional
    public boolean deleteTask(Long taskId) {
        return false;
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
}
