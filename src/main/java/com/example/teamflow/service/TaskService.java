package com.example.teamflow.service;

import com.example.teamflow.entity.Task;
import com.example.teamflow.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("該当するタスクがありません id: " + id));
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task task) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("該当するタスクがありません id: " + id));

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setProject(task.getProject());
        existingTask.setCategory(task.getCategory());
        existingTask.setPatient(task.getPatient());
        existingTask.setAssignedToAll(task.isAssignedToAll());
        existingTask.setPriority(task.getPriority());
        existingTask.setTaskStatus(task.getTaskStatus());
        existingTask.setDueDate(task.getDueDate());

        // TODO: グローバルエラーハンドリング実装時に改善する
        return taskRepository.save(existingTask);
    }

    public String deleteTask(Long id) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("該当するタスクがありません id: " + id));

        existingTask.setDeletedAt(LocalDateTime.now());
        taskRepository.save(existingTask);
        return "task_id: " + id + " 削除しました";
    }
}
