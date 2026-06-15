package com.example.teamflow.dto;

import com.example.teamflow.entity.TaskSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryResponse {
    private Long id;
    private Long patientId;
    private String summary;
    private LocalDateTime generatedAt;
    private String generatedByName;

    public static TaskSummaryResponse from(TaskSummary summary) {
        TaskSummaryResponse dto = new TaskSummaryResponse();
        dto.id = summary.getId();
        dto.patientId = summary.getPatient().getId();
        dto.summary = summary.getSummary();
        dto.generatedAt = summary.getGeneratedAt();
        dto.generatedByName = summary.getGeneratedBy().getLastName()
                + " "
                + summary.getGeneratedBy().getFirstName();
        return dto;
    }
}