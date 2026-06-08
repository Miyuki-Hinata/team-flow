package com.example.teamflow.repository;

import com.example.teamflow.entity.RefreshToken;
import com.example.teamflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    // 導出削除クエリ（deleteBy〇〇）はentity.remove()を呼ぶためトランザクションが必須
    @Transactional
    void deleteByUser(User user);
}
