package com.example.teamflow.service;

import com.example.teamflow.entity.Announcement;
import com.example.teamflow.entity.AnnouncementRead;
import com.example.teamflow.entity.User;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.AnnouncementReadRepository;
import com.example.teamflow.repository.AnnouncementRepository;
import com.example.teamflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
        // exists 系にすることで、レガシーな重複行があっても NonUniqueResultException を起こさない
        return announcementReadRepository.existsByAnnouncementIdAndUserId(announcementId, userId);
    }

    public void markAsRead(Long announcementId, Long userId) {
        // 既に既読なら何もしない（並行呼び出しでの二重 INSERT は最終的に DB のユニーク制約で弾く）
        if (isRead(announcementId, userId)) return;

        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("お知らせが見つかりません"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));

        AnnouncementRead read = new AnnouncementRead();
        read.setAnnouncement(announcement);
        read.setUser(user);
        try {
            announcementReadRepository.save(read);
        } catch (DataIntegrityViolationException e) {
            // 並行呼び出し（React StrictMode の useEffect 二重発火 / 一覧＋詳細の同時 markAsRead 等）で
            // isRead チェックの隙間に別リクエストが先に INSERT した場合、ユニーク制約違反になる。
            // 冪等な操作として扱い、既読状態が達成されていれば成功とみなす
        }
    }

}
