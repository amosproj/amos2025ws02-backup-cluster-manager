package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.model.database.Task;
import com.bcm.shared.model.database.TaskFrequency;
import com.bcm.shared.pagination.sort.TaskComparators;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.util.NodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;


@Service
public class CMTaskService implements PaginationProvider<TaskDTO> {

    @Autowired
    private RegistryService registryService;

    @Autowired
    private RestTemplate restTemplate;


    @Override
    public long getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        List<TaskDTO> base = (getAllTasks());
        return applySearch(applyFilters(base, filter), filter).size();

    }

    @Override
    public List<TaskDTO> getDBItems(long page, long itemsPerPage, Filter filter) {
        List<TaskDTO> allBackups = (getAllTasks());

        List<TaskDTO> filtered = applyFilters(allBackups, filter);
        List<TaskDTO> searched = applySearch(filtered, filter);
        List<TaskDTO> sorted = SortProvider.sort(
                searched,
                filter.getSortBy(),
                filter.getSortOrder() != null ? filter.getSortOrder().toString() : null,
                TaskComparators.COMPARATORS
        );
        int fromIndex = (int) Math.max(0, (page - 1) * itemsPerPage);
        int toIndex = Math.min(fromIndex + (int) itemsPerPage, sorted.size());
        if (fromIndex >= toIndex) {
            return new ArrayList<>();
        }
        return sorted.subList(fromIndex, toIndex);
    }

    //TODO: functionality to be tested, just a draft
    public List<TaskDTO> getAllTasks() {
        List<String> nodeAddresses = NodeUtils.addresses(registryService.getActiveNodes());
        if (nodeAddresses.isEmpty()) return List.of();

        List<CompletableFuture<TaskDTO[]>> futures = nodeAddresses.stream().map(address -> CompletableFuture.supplyAsync(() -> {
                    try {
                        String url = "http://" + address + "/api/v1/bn/tasks";
                        ResponseEntity<TaskDTO[]> response = restTemplate.getForEntity(url, TaskDTO[].class);
                        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                            return response.getBody();
                        }
                    } catch (Exception e) {
                        System.out.println("Fehler beim Abruf von Tasks von Node " + address + ". Error: " + (e.getMessage()));
                    }
                    return null;
                })).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<TaskDTO> allTasks = new ArrayList<>();
        //Set<Long> seenIds = new HashSet<>();

        for (CompletableFuture<TaskDTO[]> future : futures) {
            try {
                TaskDTO[] tasks = future.get();
                if (tasks != null) {
                    for (TaskDTO task : tasks) {
                        //if (task != null && seenIds.add(task.getId())) {
                            allTasks.add(task);
                        //}
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return allTasks;
    }

    // Filters by BackupState parsed from filter.getFilters()
    private List<TaskDTO> applyFilters(List<TaskDTO> tasks, Filter filter) {
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
    private List<TaskDTO> applySearch(List<TaskDTO> backups, Filter filter) {
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
    public TaskDTO addTask(TaskDTO task) {
        List<String> nodeAddresses = NodeUtils.addresses(registryService.getActiveNodes());
        if (nodeAddresses.isEmpty()) return null;

        if (task == null || task.getClientId() == null) {
            return null;
        }

        List<CompletableFuture<TaskDTO>> futures = nodeAddresses.stream()
                .map(address -> CompletableFuture.supplyAsync(() -> {
                    try {
                        String url = "http://" + address + "/api/v1/bn/task";
                        ResponseEntity<TaskDTO> response = restTemplate.postForEntity(url, task, TaskDTO.class);
                        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                            return response.getBody();
                        }
                    } catch (Exception e) {
                        System.out.println("Fehler beim Hinzufügen von Task an Node " + address);
                    }
                    return null;
                })).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (CompletableFuture<TaskDTO> future : futures) {
            try {
                TaskDTO result = future.get();
                if (result != null) {
                    return result;
                }
            } catch (Exception ignored) {
            }
        }
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
