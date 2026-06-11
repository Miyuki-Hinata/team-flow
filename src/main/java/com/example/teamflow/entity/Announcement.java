package com.example.teamflow.entity;

import com.example.teamflow.enums.Priority;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

import static com.example.teamflow.enums.Priority.MEDIUM;

@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
public class Announcement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "タイトルを入力してください")
    private String title;

    private String description;

    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "fk_announcement_project_id"))
    private Project project;

    @ManyToOne
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_announcement_category_id"))
    private Category category;

    @ManyToOne
    @JoinColumn(name ="department_id", foreignKey = @ForeignKey(name = "fk_annoucement_department_id"))
    private Department department;

    @NotNull(message = "優先度を入力してください")
    private Priority priority = MEDIUM;

    private LocalDateTime expiredAt;

    @ManyToOne
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_announcement_created_by"))
    private User createdBy;
}

//■ Announcement（お知らせ）
//        - id            BIGINT    PK
//- title         VARCHAR   タイトル（NOT NULL）
//        - description   TEXT      詳細（NULL可）
//        - project_id    BIGINT    FK → projects.id（NULL可）
//        - category_id   BIGINT    FK → categories.id（NULL可）
//        - department_id BIGINT    FK → departments.id（NULL可）
//        - priority      ENUM      LOW / MEDIUM / HIGH
//- expired_at    DATETIME  掲載終了日時（NULL可）
//        + BaseEntity共通カラム