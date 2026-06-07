package com.example.teamflow.dto;

import com.example.teamflow.entity.TaskHistory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TaskHistoryResponse {

    private Long id;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;
    private UserResponse changedBy;

    public static TaskHistoryResponse from(TaskHistory history) {
        TaskHistoryResponse dto = new TaskHistoryResponse();
        dto.id = history.getId();
        dto.fieldName = history.getFieldName();
        dto.oldValue = history.getOldValue();
        dto.newValue = history.getNewValue();
        dto.changedAt = history.getChangedAt();
        dto.changedBy = UserResponse.from(history.getChangedBy());
        return dto;
    }
}
