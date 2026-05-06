package com.example.teamflow.dto;

import com.example.teamflow.enums.Priority;
import com.example.teamflow.enums.TaskStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TaskRequest {
    @NotEmpty(message = "タイトルを入力してください")
    private String title;

    private String description;
    private Long projectId;
    private Long categoryId;
    private Long patientId;
    private boolean assignedToAll;

    @NotNull(message = "優先度を入力してください")
    private Priority priority;

    @NotNull(message = "ステータスを入力してください")
    private TaskStatus taskStatus;

    private LocalDateTime dueDate;
    private List<Long> assigneeIds;
}
