package com.example.teamflow.repository;

import com.example.teamflow.entity.UserPatientAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPatientAssignmentRepository extends JpaRepository<UserPatientAssignment, Long> {

    // あるユーザーの受け持ち割り当てを全件取得する（GET /api/me/assigned-patients 用）
    List<UserPatientAssignment> findByUserId(Long userId);

    // あるユーザーの割り当てを全削除する。
    // PUT の「集合置換」で、新しい集合を入れる前に一旦まっさらにする用途。
    // 派生 delete クエリは対象を取得してから削除するため、呼び出し側は @Transactional の中で使う。
    void deleteByUserId(Long userId);
}
