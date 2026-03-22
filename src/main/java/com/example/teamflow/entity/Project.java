package com.example.teamflow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class Project extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "プロジェクト名を入力してください")
    @Column(name = "project_name")
    private String projectName;

    @ManyToOne
    @NotNull(message = "部署IDを入力してください")
    @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_projects_department_id"))
    private Department department;
}


//projects
//    - id (PK)
//    - プロジェクト名
//    - department_id (FK)
//    - 作成日時
//    - 削除日時
//    - ステータス（論理削除）