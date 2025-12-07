package com.bcm.shared.service;

import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.model.database.Task;
import com.bcm.shared.model.database.TaskFrequency;
import com.bcm.shared.repository.TaskMapper;
import com.bcm.shared.service.sort.TaskComparators;
import com.bcm.shared.filter.Filter;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.sort.SortProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


@Service
public class TaskService implements PaginationProvider<TaskDTO> {

    @Autowired
    private TaskMapper taskMapper;

    /**
     * Pagination
     */

    @Override
    public long getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        List<TaskDTO> base = (getAllBackups());
        return applySearch(applyFilters(base, filter), filter).size();

    }

    @Override
    public List<TaskDTO> getDBItems(long page, long itemsPerPage, Filter filter) {
        List<TaskDTO> allBackups = (getAllBackups());

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

    public List<TaskDTO> getAllBackups() {
        List<Task> tasks = taskMapper.findAll();
        List<TaskDTO> taskDTOS = new ArrayList<>();

        for (Task task : tasks) {
            taskDTOS.add(toDto(task));
        }
        return taskDTOS;

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
    public TaskDTO addTask(Task task) {
        taskMapper.insert(task);
        return toDto(taskMapper.findById(task.getId()));
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
