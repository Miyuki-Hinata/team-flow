package com.example.teamflow.service;

import com.example.teamflow.dto.TaskRequest;
import com.example.teamflow.entity.Category;
import com.example.teamflow.entity.Patient;
import com.example.teamflow.entity.Project;
import com.example.teamflow.entity.Task;
import com.example.teamflow.entity.User;
import com.example.teamflow.enums.TaskStatus;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.CategoryRepository;
import com.example.teamflow.repository.PatientRepository;
import com.example.teamflow.repository.ProjectRepository;
import com.example.teamflow.repository.TaskRepository;
import com.example.teamflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    // 全件取得
    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    // 削除済み除外
    public List<Task> getActiveTasks() {
        return taskRepository.findByDeletedAtIsNull();
    }

    // 自分の担当タスク取得
    public List<Task> getMyTasks(Long id) {
        return taskRepository.findByAssignees_IdAndDeletedAtIsNull(id);
    }

    // 患者別のタスクを取得
    public List<Task> getTasksByPatientId(Long id) {
        return taskRepository.findByPatient_IdAndDeletedAtIsNull(id);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するタスクがありません id: " + id));
    }

    public Task createTask(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignedToAll(request.isAssignedToAll());
        task.setPriority(request.getPriority());
        task.setTaskStatus(
                request.getTaskStatus() == null ? TaskStatus.CREATED : request.getTaskStatus()
        );
        task.setDueDate(request.getDueDate());

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当するプロジェクトがありません"));
            task.setProject(project);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当するカテゴリーがありません"));
            task.setCategory(category);
        }

        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当する患者がありません"));
            task.setPatient(patient);
        }

        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            List<User> assignees = request.getAssigneeIds().stream()
                    .map(userId -> userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("該当するユーザーがありません")))
                    .collect(Collectors.toList());
            task.setAssignees(assignees);
        }

        return taskRepository.save(task);
    }

    public Task updateTask(Long id, TaskRequest request) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するタスクがありません id: " + id));

        existingTask.setTitle(request.getTitle());
        existingTask.setDescription(request.getDescription());
        existingTask.setAssignedToAll(request.isAssignedToAll());
        existingTask.setPriority(request.getPriority());
        existingTask.setTaskStatus(request.getTaskStatus());
        existingTask.setDueDate(request.getDueDate());

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当するプロジェクトがありません"));
            existingTask.setProject(project);
        } else {
            existingTask.setProject(null);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当するカテゴリーがありません"));
            existingTask.setCategory(category);
        } else {
            existingTask.setCategory(null);
        }

        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("該当する患者がありません"));
            existingTask.setPatient(patient);
        } else {
            existingTask.setPatient(null);
        }

        if (request.getAssigneeIds() != null) {
            List<User> assignees = request.getAssigneeIds().stream()
                    .map(userId -> userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("該当するユーザーがありません")))
                    .collect(Collectors.toList());
            existingTask.setAssignees(assignees);
        }

        return taskRepository.save(existingTask);
    }

    public String deleteTask(Long id) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するタスクがありません id: " + id));

        existingTask.setDeletedAt(LocalDateTime.now());
        taskRepository.save(existingTask);
        return "task_id: " + id + " 削除しました";
    }
}
