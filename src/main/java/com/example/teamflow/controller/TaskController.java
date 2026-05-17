package com.example.teamflow.controller;

import com.example.teamflow.dto.TaskRequest;
import com.example.teamflow.entity.Task;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
        import com.example.teamflow.repository.UserRepository;

import java.util.List;

@RestController
public class TaskController {
    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/api/tasks")
    public List<Task> getTasks() {
        return taskService.getActiveTasks();
    }

    @GetMapping("/api/tasks/my-tasks")
    public List<Task> getMyTasks() {
        // JWTからログインIDを取得
        String loginId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // loginIdからuserIdを取得
        Long userId = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"))
                .getId();

        return taskService.getMyTasks(userId);
    }

    @GetMapping("/api/tasks/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PostMapping("/api/tasks")
    public Task createTask(
            @Valid @RequestBody TaskRequest request
    ) {
        return taskService.createTask(request);
    }

    @PutMapping("/api/tasks/{id}")
    public Task updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request
    ) {
        return taskService.updateTask(id, request);
    }

    @DeleteMapping("/api/tasks/{id}")
    public String deleteTask(@PathVariable Long id) {
        return taskService.deleteTask(id);
    }
}
