package com.example.teamflow.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequest {
    @NotEmpty(message = "プロジェクト名を入力してください")
    private String projectName;

    @NotNull(message = "部署IDを入力してください")
    private Long departmentId;
}
