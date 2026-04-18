package com.example.teamflow.controller;

import com.example.teamflow.entity.Announcement;
import com.example.teamflow.service.AnnouncementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("")
    public List<Announcement> getAnnouncements() {
        return announcementService.getAnnouncements();
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