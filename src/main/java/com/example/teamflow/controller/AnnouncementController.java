package com.example.teamflow.controller;

import com.example.teamflow.dto.AnnouncementHistoryResponse;
import com.example.teamflow.dto.AnnouncementRequest;
import com.example.teamflow.dto.AnnouncementResponse;
import com.example.teamflow.entity.Announcement;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.UserRepository;
import com.example.teamflow.service.AnnouncementHistoryService;
import com.example.teamflow.service.AnnouncementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private AnnouncementHistoryService announcementHistoryService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public List<AnnouncementResponse> getAnnouncements() {
        Long userId = getCurrentUserId();

        return announcementService.getAnnouncements(userId);
    }

    @GetMapping("/my")
    public List<AnnouncementResponse> getMyAnnouncements() {
        Long userId = getCurrentUserId();

        return announcementService.getMyAnnouncements(userId);
    }

    @GetMapping("/{id}/histories")
    public List<AnnouncementHistoryResponse> getAnnouncementHistories(@PathVariable Long id) {
        return announcementHistoryService.getHistoriesByAnnouncementId(id);
    }

    private Long getCurrentUserId() {
        // JWTからログインIDを取得
        String loginId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // loginIdからuserIdを取得
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"))
                .getId();
    }

    @GetMapping("/{id}")
    public AnnouncementResponse getAnnouncementById (@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return announcementService.getAnnouncementById(id, userId);
    }

    @PostMapping("")
    public Announcement createAnnouncement(
            @Valid @RequestBody AnnouncementRequest request
    ) {
        return announcementService.createAnnouncement(request);
    }

    @PutMapping("/{id}")
    public Announcement updateAnnouncement(
        @PathVariable Long id,
        @Valid @RequestBody AnnouncementRequest request
    ) {
        return announcementService.updateAnnouncement(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteAnnouncement(@PathVariable Long id) {
        return announcementService.deleteAnnouncement(id);
    }
}