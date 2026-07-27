package com.example.teamflow.repository;

import com.example.teamflow.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 論理削除されていないカテゴリだけを取得する（派生クエリ）
    List<Category> findByDeletedAtIsNull();
}
