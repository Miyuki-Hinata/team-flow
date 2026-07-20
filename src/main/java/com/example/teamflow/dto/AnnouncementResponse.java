package com.example.teamflow.dto;

import com.example.teamflow.entity.Category;
import com.example.teamflow.entity.Department;
import com.example.teamflow.entity.Project;
import com.example.teamflow.enums.Priority;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String description;
    private Project project;
    private Category category;
    private Department department;
    private Priority priority;
    private LocalDateTime expiredAt;
    private Boolean isRead;
    private UserResponse createdBy;
    // BaseEntity.createdAt をレスポンスへ含める（フロントの詳細ページ「作成日時」表示に必要）
    private LocalDateTime createdAt;
}
