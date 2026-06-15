package com.example.teamflow.controller;

import com.example.teamflow.dto.TaskSummaryResponse;
import com.example.teamflow.entity.TaskSummary;
import com.example.teamflow.service.TaskSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class TaskSummaryController {

    @Autowired
    private TaskSummaryService taskSummaryService;

    @GetMapping("/api/patients/{patientId}/summary")
    public TaskSummaryResponse getTaskSummary(@PathVariable Long patientId) {
        TaskSummary summary = taskSummaryService.getSummary(patientId);
        if (summary == null) {
            return null;
        }
        return TaskSummaryResponse.from(summary);
    }

    @PostMapping("/api/patients/{patientId}/summary")
    public TaskSummaryResponse generateTaskSummary(@PathVariable Long patientId) {
        TaskSummary summary = taskSummaryService.generateSummary(patientId);
        return TaskSummaryResponse.from(summary);
    }
}