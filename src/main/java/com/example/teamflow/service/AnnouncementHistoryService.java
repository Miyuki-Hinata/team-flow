package com.example.teamflow.service;

import com.example.teamflow.dto.AnnouncementHistoryResponse;
import com.example.teamflow.repository.AnnouncementHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementHistoryService {

    @Autowired
    private AnnouncementHistoryRepository announcementHistoryRepository;

    public List<AnnouncementHistoryResponse> getHistoriesByAnnouncementId(Long announcementId) {
        return announcementHistoryRepository.findByAnnouncement_IdOrderByChangedAtDesc(announcementId)
                .stream()
                .map(AnnouncementHistoryResponse::from)
                .collect(Collectors.toList());
    }
}
