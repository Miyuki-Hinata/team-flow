package com.example.teamflow.service;

import com.example.teamflow.dto.AnnouncementResponse;
import com.example.teamflow.entity.Announcement;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {
    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private AnnouncementReadService announcementReadService;

    public List<AnnouncementResponse> getAnnouncements(Long userId) {
        return announcementRepository.findAll().stream()
                .map(announcement -> {
                    AnnouncementResponse response = new AnnouncementResponse();
                    response.setId(announcement.getId());
                    response.setTitle(announcement.getTitle());
                    response.setDescription(announcement.getDescription());
                    response.setProject(announcement.getProject());
                    response.setCategory(announcement.getCategory());
                    response.setDepartment(announcement.getDepartment());
                    response.setPriority(announcement.getPriority());
                    response.setExpiredAt(announcement.getExpiredAt());
                    response.setIsRead(announcementReadService.isRead(announcement.getId(), userId));
                    return response;
                })
                .collect(Collectors.toList());
    }

    public Announcement getAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("該当するお知らせがありません id:" + id));
    }

    public Announcement createAnnouncement(Announcement announcement) {
        return announcementRepository.save(announcement);
    }

    public Announcement updateAnnouncement(Long id, Announcement announcement) {
        Announcement existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するお知らせがありません id: "  + id));

        existingAnnouncement.setTitle(announcement.getTitle());
        existingAnnouncement.setDescription(announcement.getDescription());
        existingAnnouncement.setProject(announcement.getProject());
        existingAnnouncement.setCategory(announcement.getCategory());
        existingAnnouncement.setPriority(announcement.getPriority());
        existingAnnouncement.setExpiredAt(announcement.getExpiredAt());

        return announcementRepository.save(existingAnnouncement);
    }

    public String deleteAnnouncement(long id) {
        Announcement existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するお知らせがありません id: " + id));

        existingAnnouncement.setDeletedAt(LocalDateTime.now());
        announcementRepository.save(existingAnnouncement);
        return "announcement_id: " + id  + "を削除しました";
    }
}