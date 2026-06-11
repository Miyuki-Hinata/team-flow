package com.example.teamflow.dto;
import com.example.teamflow.enums.Priority;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AnnouncementRequest {
    @NotEmpty
    private String title;
    private String description;
    private Long projectId;
    @NotNull(message = "カテゴリーを選択してください")
    private Long categoryId;
    private Long departmentId;
    @NotNull
    private Priority priority;
    private LocalDateTime expiredAt;
}
