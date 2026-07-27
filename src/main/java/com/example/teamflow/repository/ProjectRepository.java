package com.example.teamflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.teamflow.entity.Project;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // 論理削除されていないプロジェクトだけを取得する（派生クエリ）
    List<Project> findByDeletedAtIsNull();
}
