package com.example.teamflow.controller;

import com.example.teamflow.entity.Task;
import com.example.teamflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TaskController {
    @Autowired
    private TaskService taskService;

    @GetMapping("/api/tasks")
    public List<Task> getTasks() {
        return taskService.getTasks();
    }

    @GetMapping("/api/tasks/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PostMapping("/api/tasks")
    public Task createTask(
            @Valid @RequestBody Task task
    ) {
        return taskService.createTask(task);
    }

    @PutMapping("/api/tasks/{id}")
    public Task updateTask(
            @PathVariable Long id,
            @Valid @RequestBody Task task
    ) {
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/api/tasks/{id}")
    public String deleteTask(@PathVariable Long id) {
        return taskService.deleteTask(id);
    }
}
