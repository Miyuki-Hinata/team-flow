package com.example.teamflow.repository;

import com.example.teamflow.entity.AnnouncementRead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, Long> {
    // exists 系にすることで、万一過去データの重複が残っていても NonUniqueResultException を起こさず
    // 「1件以上あれば既読」と判定できる。読み取り性能も findBy より軽い
    boolean existsByAnnouncementIdAndUserId(Long announcementId, Long userId);
}
