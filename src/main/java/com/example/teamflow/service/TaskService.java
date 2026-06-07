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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

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
        List<TaskHistory> histories = buildHistories(task, request, newProject, newCategory, newPatient, newAssignees, changedBy);

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

    private List<TaskHistory> buildHistories(Task task, TaskRequest request,
                                              Project newProject, Category newCategory,
                                              Patient newPatient, List<User> newAssignees,
                                              User changedBy) {
        List<TaskHistory> histories = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        record(histories, task, changedBy, now, "タイトル",
                task.getTitle(), request.getTitle());

        record(histories, task, changedBy, now, "詳細",
                task.getDescription(), request.getDescription());

        record(histories, task, changedBy, now, "優先度",
                task.getPriority() != null ? task.getPriority().name() : null,
                request.getPriority() != null ? request.getPriority().name() : null);

        record(histories, task, changedBy, now, "ステータス",
                task.getTaskStatus() != null ? task.getTaskStatus().name() : null,
                request.getTaskStatus() != null ? request.getTaskStatus().name() : null);

        record(histories, task, changedBy, now, "期限",
                task.getDueDate() != null ? task.getDueDate().toString() : null,
                request.getDueDate() != null ? request.getDueDate().toString() : null);

        record(histories, task, changedBy, now, "全員に割り当て",
                String.valueOf(task.isAssignedToAll()),
                String.valueOf(request.isAssignedToAll()));

        record(histories, task, changedBy, now, "プロジェクト",
                task.getProject() != null ? task.getProject().getProjectName() : null,
                newProject != null ? newProject.getProjectName() : null);

        record(histories, task, changedBy, now, "カテゴリー",
                task.getCategory() != null ? task.getCategory().getCategoryName() : null,
                newCategory != null ? newCategory.getCategoryName() : null);

        record(histories, task, changedBy, now, "患者",
                task.getPatient() != null ? task.getPatient().getLastName() + task.getPatient().getFirstName() : null,
                newPatient != null ? newPatient.getLastName() + newPatient.getFirstName() : null);

        if (newAssignees != null) {
            String oldNames = task.getAssignees() != null
                    ? task.getAssignees().stream()
                            .sorted(Comparator.comparing(User::getId))
                            .map(u -> u.getLastName() + u.getFirstName())
                            .collect(Collectors.joining(", "))
                    : "";
            String newNames = newAssignees.stream()
                    .sorted(Comparator.comparing(User::getId))
                    .map(u -> u.getLastName() + u.getFirstName())
                    .collect(Collectors.joining(", "));
            record(histories, task, changedBy, now, "担当者", oldNames, newNames);
        }

        return histories;
    }

    private void record(List<TaskHistory> histories, Task task, User changedBy,
                        LocalDateTime changedAt, String fieldName,
                        String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            TaskHistory h = new TaskHistory();
            h.setTask(task);
            h.setChangedBy(changedBy);
            h.setChangedAt(changedAt);
            h.setFieldName(fieldName);
            h.setOldValue(oldValue);
            h.setNewValue(newValue);
            histories.add(h);
        }
    }

    public String deleteTask(Long id) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するタスクがありません id: " + id));

        existingTask.setDeletedAt(LocalDateTime.now());
        taskRepository.save(existingTask);
        return "task_id: " + id + " 削除しました";
    }
}
