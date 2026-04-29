package com.example.teamflow.service;

import com.example.teamflow.entity.Announcement;
import com.example.teamflow.entity.AnnouncementRead;
import com.example.teamflow.entity.User;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.AnnouncementReadRepository;
import com.example.teamflow.repository.AnnouncementRepository;
import com.example.teamflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementReadService {
    @Autowired
    private AnnouncementReadRepository announcementReadRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;
    @Autowired
    private UserRepository userRepository;

    public boolean isRead(Long announcementId, Long userId) {
        return announcementReadRepository
                .findByAnnouncementIdAndUserId(announcementId, userId)
                .isPresent();
    }

    public void markAsRead(Long announcementId, Long userId) {
        // 既に既読なら何もしない
        if (isRead(announcementId, userId)) return;

        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("お知らせが見つかりません"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));

        // 新しい既読レコードを作成
        AnnouncementRead read = new AnnouncementRead();
        read.setAnnouncement(announcement);
        read.setUser(user);
        announcementReadRepository.save(read);
    }

}
