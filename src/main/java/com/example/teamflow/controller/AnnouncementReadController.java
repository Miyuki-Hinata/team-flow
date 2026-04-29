package com.example.teamflow.controller;

import com.example.teamflow.repository.UserRepository;
import com.example.teamflow.service.AnnouncementReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementReadController {
    @Autowired
    private AnnouncementReadService announcementReadService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        // JWTからログインIDを取得
        String loginId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // loginIdからuserを検索
        Long userId = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("該当するユーザーが見つかりません"))
                .getId();

        // 既読にする
        announcementReadService.markAsRead(id, userId);
    }

}
