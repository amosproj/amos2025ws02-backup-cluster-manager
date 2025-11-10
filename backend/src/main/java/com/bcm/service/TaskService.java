package com.bcm.service;

import com.bcm.domain.Client;
import com.bcm.domain.Task;
import com.bcm.repository.ClientRepository;
import com.bcm.repository.TaskRepository;
import com.bcm.service.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TaskService {

    private final TaskRepository tasks;
    private final ClientRepository clients;

    public TaskService(TaskRepository tasks, ClientRepository clients) {
        this.tasks = tasks;
        this.clients = clients;
    }

    public Task create(Task t, UUID clientId) {
        if (t.getId() == null) t.setId(UUID.randomUUID());
        Client c = clients.findById(clientId).orElseThrow(() ->
                new NotFoundException("Client %s not found".formatted(clientId)));
        t.setClient(c);
        return tasks.save(t);
    }

    @Transactional(readOnly = true)
    public Task get(UUID id) {
        return tasks.findById(id).orElseThrow(() ->
                new NotFoundException("Task %s not found".formatted(id)));
    }

    @Transactional(readOnly = true)
    public List<Task> listByClient(UUID clientId) {
        return tasks.findByClient_Id(clientId);
    }

    public Task update(UUID id, Task patch, UUID newClientId) {
        Task t = get(id);
        if (patch.getName() != null) t.setName(patch.getName());
        if (patch.getSource() != null) t.setSource(patch.getSource());
        t.setEnabled(patch.isEnabled());

        if (newClientId != null) {
            Client c = clients.findById(newClientId).orElseThrow(() ->
                    new NotFoundException("Client %s not found".formatted(newClientId)));
            t.setClient(c);
        }
        return tasks.save(t);
    }

    public void delete(UUID id) { tasks.deleteById(id); }
}
