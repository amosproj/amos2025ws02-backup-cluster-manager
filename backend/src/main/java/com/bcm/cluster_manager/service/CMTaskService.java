package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.BigTaskDTO;
import com.bcm.cluster_manager.service.pagination.shared.BigTaskComparators;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.model.database.Task;
import com.bcm.shared.model.database.TaskFrequency;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.sort.SortProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;


@Service
public class CMTaskService implements PaginationProvider<BigTaskDTO> {

    private static final Logger logger = LoggerFactory.getLogger(CMTaskService.class);
    private static final String TASKS_ENDPOINT = "/api/v1/bn/tasks";
    private static final String TASK_ENDPOINT = "/api/v1/bn/task";

    @Autowired
    private RegistryService registryService;

    private final WebClient webClient;
    private final CacheManager cacheManager;

    public CMTaskService(WebClient.Builder webClientBuilder, CacheManager cacheManager) {
        this.webClient = webClientBuilder.build();
        this.cacheManager = cacheManager;
    }


    @Override
    public Mono<Long> getTotalItemsCount(Filter filter) {
        return getAllTasksReactive()
                .map(list -> (long) applySearch(applyFilters(list, filter), filter).size());
    }

    @Override
    public Mono<List<BigTaskDTO>> getDBItems(long page, long itemsPerPage, Filter filter) {

        String cacheKey = buildCacheKey(page, itemsPerPage, filter);  // Use stable key

        List<BigTaskDTO> cached = cacheManager.getCache("taskPages").get(cacheKey, List.class);
        if (cached != null) {
            logger.info("Page cache HIT: {}", cacheKey);
            return Mono.just(cached);
        }

        logger.info("Page cache MISS: {}", cacheKey);

        return getAllTasksReactive()
                .map(allTasks -> {
                    List<BigTaskDTO> filtered = applyFilters(allTasks, filter);
                    List<BigTaskDTO> searched = applySearch(filtered, filter);
                    List<BigTaskDTO> sorted = SortProvider.sort(
                            searched,
                            filter.getSortBy(),
                            filter.getSortOrder() != null ? filter.getSortOrder().toString() : null,
                            BigTaskComparators.COMPARATORS
                    );

                    int fromIndex = (int) Math.max(0, (page - 1) * itemsPerPage);
                    int toIndex = Math.min(fromIndex + (int) itemsPerPage, sorted.size());
                    if (fromIndex >= toIndex) return List.of();

                    List<BigTaskDTO> result = sorted.subList(fromIndex, toIndex);

                    // Cache the page result
                    cacheManager.getCache("taskPages").put(cacheKey, result);
                    logger.info("Cached page {} ({} items)", page, result.size());
                    return result;

                });
    }

    public Mono<List<BigTaskDTO>> getAllTasksReactive() {
        Collection<NodeDTO> nodes = registryService.getActiveAndManagedNodes();
        if (nodes.isEmpty()) return Mono.just(List.of());

        return Flux.fromIterable(nodes)
                .flatMap(this::fetchTasksFromNode)
                .collectList();
    }

    private Flux<BigTaskDTO> fetchTasksFromNode(NodeDTO node) {
        String url = "http://" + node.getAddress() + TASKS_ENDPOINT;

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(TaskDTO[].class)
                .timeout(Duration.ofSeconds(10))
                .flatMapMany(Flux::fromArray)
                .map(taskDto -> {
                    BigTaskDTO bigTaskDto = new BigTaskDTO();
                    bigTaskDto.setId(taskDto.getId());
                    bigTaskDto.setName(taskDto.getName());
                    bigTaskDto.setClientId(taskDto.getClientId());
                    bigTaskDto.setEnabled(taskDto.isEnabled());
                    bigTaskDto.setInterval(taskDto.getInterval());
                    bigTaskDto.setSource(taskDto.getSource());
                    bigTaskDto.setNodeDTO(node);
                    return bigTaskDto;
                })
                .onErrorResume(e -> {
                    logger.info("Fehler beim Abruf von Tasks von Node " + node.getAddress() + ". Error: " + e.getMessage());
                    return Flux.empty();
                });
    }

    public List<BigTaskDTO> getAllTasks() {
        return getAllTasksReactive().block();
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
        return addTaskReactive(task).block();
    }

    public Mono<BigTaskDTO> addTaskReactive(BigTaskDTO task) {
        if (task == null || task.getNodeDTO() == null || task.getNodeDTO().getId() == null) {
            return Mono.empty();
        }

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setName(task.getName());
        taskDTO.setClientId(task.getClientId());
        taskDTO.setSource(task.getSource());
        taskDTO.setEnabled(task.isEnabled());
        taskDTO.setInterval(task.getInterval());

        Long targetNodeId = task.getNodeDTO().getId();

        Optional<NodeDTO> targetNode = registryService.getActiveAndManagedNodes().stream()
                .filter(node -> node.getId().equals(targetNodeId))
                .findFirst();

        if (targetNode.isEmpty()) {
            logger.error("Target node for new Task not found.");
            return Mono.empty();
        }

        NodeDTO node = targetNode.get();
        String url = "http://" + node.getAddress() + TASK_ENDPOINT;

        return webClient.post()
                .uri(url)
                .bodyValue(taskDTO)
                .retrieve()
                .bodyToMono(TaskDTO.class)
                .timeout(Duration.ofSeconds(10))
                .map(dto -> {
                    BigTaskDTO createdTask = new BigTaskDTO();
                    createdTask.setId(dto.getId());
                    createdTask.setName(dto.getName());
                    createdTask.setClientId(dto.getClientId());
                    createdTask.setSource(dto.getSource());
                    createdTask.setEnabled(dto.isEnabled());
                    createdTask.setInterval(dto.getInterval());
                    createdTask.setNodeDTO(node);
                    return createdTask;
                })
                .doOnNext(created -> {
                    // Invalidate cache after successful task addition
                    cacheManager.getCache("taskPages").clear();
                    logger.info("Cache 'taskPages' invalidated after adding task {}", created.getId());
                })
                .doOnError(e -> logger.error("Fehler beim Hinzufügen von Task an Node " + node.getAddress() + ": " + e.getMessage()))
                .onErrorResume(e -> Mono.empty());
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

    private String buildCacheKey(long page, long itemsPerPage, Filter filter) {
        StringBuilder key = new StringBuilder();
        key.append("page-").append(page);
        key.append("-size-").append(itemsPerPage);

        // Add filter components
        if (filter != null) {
            if (filter.getFilters() != null && !filter.getFilters().isEmpty()) {
                key.append("-filters-").append(String.join(",", filter.getFilters().stream().sorted().toList()));
            }
            if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
                key.append("-search-").append(filter.getSearch());
            }
            if (filter.getSortBy() != null) {
                key.append("-sort-").append(filter.getSortBy());
            }
            if (filter.getSortOrder() != null) {
                key.append("-order-").append(filter.getSortOrder());
            }
        }

        return key.toString();
    }
}
