package com.example.teamflow.repository;

import com.example.teamflow.entity.AnnouncementHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementHistoryRepository extends JpaRepository<AnnouncementHistory, Long> {
    List<AnnouncementHistory> findByAnnouncement_IdOrderByChangedAtDesc(Long announcementId);
}
