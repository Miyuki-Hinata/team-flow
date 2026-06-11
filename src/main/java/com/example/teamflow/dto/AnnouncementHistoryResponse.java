package com.example.teamflow.dto;

import com.example.teamflow.entity.AnnouncementHistory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AnnouncementHistoryResponse {

    private Long id;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;
    private UserResponse changedBy;

    public static AnnouncementHistoryResponse from(AnnouncementHistory history) {
        AnnouncementHistoryResponse dto = new AnnouncementHistoryResponse();
        dto.id = history.getId();
        dto.fieldName = history.getFieldName();
        dto.oldValue = history.getOldValue();
        dto.newValue = history.getNewValue();
        dto.changedAt = history.getChangedAt();
        dto.changedBy = UserResponse.from(history.getChangedBy());
        return dto;
    }
}
