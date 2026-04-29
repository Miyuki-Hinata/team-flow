package com.example.teamflow.controller;

import com.example.teamflow.dto.AnnouncementResponse;
import com.example.teamflow.entity.Announcement;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.UserRepository;
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
    private UserRepository userRepository;

    @GetMapping("")
    public List<AnnouncementResponse> getAnnouncements() {
        // JWTからログインIDを取得
        String loginId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // loginIdからuserIdを取得
        Long userId = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"))
                .getId();

        return announcementService.getAnnouncements(userId);
    }

    @GetMapping("/{id}")
    public Announcement getAnnouncementById (@PathVariable Long id) {
        return announcementService.getAnnouncementById(id);
    }

    @PostMapping("")
    public Announcement createAnnouncement(
            @Valid @RequestBody Announcement announcement
    ) {
        return announcementService.createAnnouncement(announcement);
    }

    @PutMapping("/{id}")
    public Announcement updateAnnouncement(
        @PathVariable Long id,
        @Valid @RequestBody Announcement announcement
    ) {
        return announcementService.updateAnnouncement(id, announcement);
    }

    @DeleteMapping("/{id}")
    public String deleteAnnouncement(@PathVariable Long id) {
        return announcementService.deleteAnnouncement(id);
    }
}