package com.example.teamflow.repository;

import com.example.teamflow.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // 論理削除されていない（deletedAt が NULL の）部署だけを取得する。
    // メソッド名から Spring Data JPA が自動でクエリを組み立てる（派生クエリ）。
    List<Department> findByDeletedAtIsNull();
}
