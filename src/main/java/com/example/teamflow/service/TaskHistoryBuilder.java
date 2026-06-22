package com.example.teamflow.service;

import com.example.teamflow.dto.TaskRequest;
import com.example.teamflow.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// 差分検知だけを担う部品。Repositoryを持たない（外部依存なし）
@Component
public class TaskHistoryBuilder {
    public List<TaskHistory> buildHistories(Task task, TaskRequest request,
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

}
