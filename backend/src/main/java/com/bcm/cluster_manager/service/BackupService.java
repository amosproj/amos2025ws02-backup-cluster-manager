package com.bcm.cluster_manager.service;

import java.util.*;

import com.bcm.shared.filter.Filter;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.database.Backup;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.repository.BackupMapper;
import com.bcm.shared.sort.BackupComparators;
import com.bcm.shared.sort.SortProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.StringUtils;

import static com.bcm.shared.service.BackupStorageService.toLdt;

@Profile("cluster_manager")
@Service
public class BackupService implements PaginationProvider<BackupDTO> {

    private final RegistryService registryService;
    private final RestTemplate restTemplate;
    private final BackupMapper backupMapper;

    public BackupService(RegistryService registryService,
                         BackupMapper backupMapper,
                         RestTemplate restTemplate) {
        this.registryService = registryService;
        this.backupMapper = backupMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public long getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        List<BackupDTO> base = (getAllBackups());
        return applySearch(applyFilters(base, filter), filter).size();

    }

    @Override
    public List<BackupDTO> getDBItems(long page, long itemsPerPage, Filter filter) {
        List<BackupDTO> allBackups = (getAllBackups());

        List<BackupDTO> filtered = applyFilters(allBackups, filter);
        List<BackupDTO> searched = applySearch(filtered, filter);
        List<BackupDTO> sorted = SortProvider.sort(
                searched,
                filter.getSortBy(),
                filter.getSortOrder() != null ? filter.getSortOrder().toString() : null,
                BackupComparators.COMPARATORS
        );
        int fromIndex = (int) Math.max(0, (page - 1) * itemsPerPage);
        int toIndex = Math.min(fromIndex + (int) itemsPerPage, sorted.size());
        if (fromIndex >= toIndex) {
            return new ArrayList<>();
        }
        return sorted.subList(fromIndex, toIndex);
    }

    public List<BackupDTO> getAllBackups() {
        List<Backup> backups =  backupMapper.findAll();
        List<BackupDTO> backupDTOs = new ArrayList<>();

        for (Backup backup : backups) {
            BackupDTO dto = new BackupDTO(
                    backup.getId(),
                    backup.getClientId(),
                    backup.getTaskId(),
                    backup.getMessage(),
                    backup.getState(),
                    backup.getSizeBytes(),
                    toLdt(backup.getStartTime()),
                    toLdt(backup.getStopTime()),
                    toLdt(backup.getCreatedAt()),
                    null
            );
            backupDTOs.add(dto);
        }

        return backupDTOs;

    }

    // Filters by BackupState parsed from filter.getFilters()
    private List<BackupDTO> applyFilters(List<BackupDTO> backups, Filter filter) {
        if (filter == null || filter.getFilters() == null || filter.getFilters().isEmpty()) {
            return backups;
        }

        var requested = filter.getFilters().stream()
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(s -> {
                    try { return BackupState.valueOf(s); }
                    catch (IllegalArgumentException ex) { return null; }
                })
                .filter(state -> state != null)
                .distinct()
                .toList();

        if (requested.isEmpty()) return backups;

        if (requested.size() == BackupState.values().length) return backups;

        return backups.stream()
                .filter(b -> requested.contains(b.getState()))
                .toList();
    }

    // Searches in name, clientId, taskId, state name, and id (if present)
    private List<BackupDTO> applySearch(List<BackupDTO> backups, Filter filter) {
        if (filter != null && StringUtils.hasText(filter.getSearch())) {
            String searchTerm = filter.getSearch().toLowerCase();
            return backups.stream()
                    .filter(b ->
                            (b.getName() != null && b.getName().toLowerCase().contains(searchTerm)) ||
                                    (b.getClientId() != null && b.getClientId().toString().toLowerCase().contains(searchTerm)) ||
                                    (b.getTaskId() != null && b.getTaskId().toString().toLowerCase().contains(searchTerm)) ||
                                    (b.getState() != null && b.getState().name().toLowerCase().contains(searchTerm)) ||
                                    (b.getId() != null && b.getId().toString().toLowerCase().contains(searchTerm))
                    )
                    .toList();
        }
        return backups;
    }

}
