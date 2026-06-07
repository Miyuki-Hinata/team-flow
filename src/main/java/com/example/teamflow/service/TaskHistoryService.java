package com.example.teamflow.service;

import com.example.teamflow.dto.TaskHistoryResponse;
import com.example.teamflow.repository.TaskHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskHistoryService {

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    public List<TaskHistoryResponse> getHistoriesByTaskId(Long taskId) {
        return taskHistoryRepository.findByTask_IdOrderByChangedAtDesc(taskId)
                .stream()
                .map(TaskHistoryResponse::from)
                .collect(Collectors.toList());
    }
}
