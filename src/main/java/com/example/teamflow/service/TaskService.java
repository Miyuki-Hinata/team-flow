package com.example.teamflow.service;

import com.example.teamflow.dto.TaskRequest;
import com.example.teamflow.entity.Category;
import com.example.teamflow.entity.Patient;
import com.example.teamflow.entity.Project;
import com.example.teamflow.entity.Task;
import com.example.teamflow.entity.TaskHistory;
import com.example.teamflow.entity.User;
import com.example.teamflow.enums.TaskStatus;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.CategoryRepository;
import com.example.teamflow.repository.PatientRepository;
import com.example.teamflow.repository.ProjectRepository;
import com.example.teamflow.repository.TaskHistoryRepository;
import com.example.teamflow.repository.TaskRepository;
import com.example.teamflow.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final TaskHistoryBuilder taskHistoryBuilder;

    public TaskService(TaskRepository taskRepository,
                       CategoryRepository categoryRepository,
                       ProjectRepository projectRepository,
                       PatientRepository patientRepository,
                       UserRepository userRepository,
                       TaskHistoryRepository taskHistoryRepository,
                       TaskHistoryBuilder taskHistoryBuilder) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.projectRepository = projectRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.taskHistoryRepository = taskHistoryRepository;
        this.taskHistoryBuilder = taskHistoryBuilder;
    }

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
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("該当するタスクがありません id: " + id));

        // 操作ユーザーを取得
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        User changedBy = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));

        // 新しい関連エンティティを先に解決（差分検知で使うため）
        Project newProject = request.getProjectId() != null
                ? projectRepository.findById(request.getProjectId())
                        .orElseThrow(() -> new ResourceNotFoundException("該当するプロジェクトがありません"))
                : null;

        Category newCategory = request.getCategoryId() != null
                ? categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("該当するカテゴリーがありません"))
                : null;

        Patient newPatient = request.getPatientId() != null
                ? patientRepository.findById(request.getPatientId())
                        .orElseThrow(() -> new ResourceNotFoundException("該当する患者がありません"))
                : null;

        List<User> newAssignees = null;
        if (request.getAssigneeIds() != null) {
            newAssignees = request.getAssigneeIds().stream()
                    .map(userId -> userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("該当するユーザーがありません")))
                    .collect(Collectors.toList());
        }

        // 変更前後の差分を履歴レコードとして生成
        List<TaskHistory> histories = taskHistoryBuilder.buildHistories(task, request, newProject, newCategory, newPatient, newAssignees, changedBy);

        // タスクを更新
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignedToAll(request.isAssignedToAll());
        task.setPriority(request.getPriority());
        task.setTaskStatus(request.getTaskStatus());
        task.setDueDate(request.getDueDate());
        task.setProject(newProject);
        task.setCategory(newCategory);
        task.setPatient(newPatient);
        if (newAssignees != null) {
            task.setAssignees(newAssignees);
        }

        Task savedTask = taskRepository.save(task);

        if (!histories.isEmpty()) {
            taskHistoryRepository.saveAll(histories);
        }

        return savedTask;
    }

    public String deleteTask(Long id) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するタスクがありません id: " + id));

        existingTask.setDeletedAt(LocalDateTime.now());
        taskRepository.save(existingTask);
        return "task_id: " + id + " 削除しました";
    }
}
