package com.bcm.shared.service;

import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.model.database.Task;
import com.bcm.shared.model.database.TaskFrequency;
import com.bcm.shared.repository.TaskMapper;
import com.bcm.shared.pagination.sort.TaskComparators;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.sort.SortProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;


@Service
public class TaskService implements PaginationProvider<TaskDTO> {

    @Autowired
    private ClientService clientService;

    private final TaskMapper taskMapper;

    public TaskService(@Qualifier("taskMapperBN") TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    /**
     * Pagination
     */

    @Override
    public Mono<Long> getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        return getAllTasks()
                .map(list -> (long) applySearch(applyFilters(list, filter), filter).size());
    }

    @Override
    public Mono<List<TaskDTO>> getDBItems(long page, long itemsPerPage, Filter filter) {

        return getAllTasks()
                .map(allTasks -> {

                    List<TaskDTO> filtered = applyFilters(allTasks, filter);
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
                });
    }

    public Mono<List<TaskDTO>> getAllTasks() {
        return taskMapper.findAllTasks()
                .map(this::toDto)
                .collectList();
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
    public Mono<TaskDTO> createTask(Task task) {
        //System.out.println("Creating task for clientId={}" + task.getClientId());

        return clientService.getClientById(task.getClientId())
                .flatMap(client ->
                        taskMapper.save(task).map(this::toDto)
                )
                .switchIfEmpty(Mono.empty());
    }

    @Transactional
    public Mono<Task> editTask(Task task) {
        return taskMapper.save(task);
    }

    @Transactional
    public Mono<Void> deleteTask(Long taskId) {
        return taskMapper.deleteById(taskId);
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
