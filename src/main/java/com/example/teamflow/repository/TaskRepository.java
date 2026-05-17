package com.example.teamflow.repository;

import com.example.teamflow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // deletedAt が null のものだけ取得
    List<Task> findByDeletedAtIsNull();
    Optional<Task> findByIdAndDeletedAtIsNull(Long id);

    // 自分の担当タスク一覧を取得
    List<Task> findByAssignees_IdAndDeletedAtIsNull(Long userId);
}