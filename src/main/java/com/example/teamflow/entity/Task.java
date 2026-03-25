package com.example.teamflow.entity;

import com.example.teamflow.enums.Priority;
import com.example.teamflow.enums.TaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class Task extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "タイトルを入力してください")
    private String title;

    private String description;

    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "fk_tasks_project_id"))
    private Project project;

    @ManyToOne
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_tasks_category_id"))
    private Category category;

    @ManyToOne
    @JoinColumn(name = "patient_id", foreignKey = @ForeignKey(name = "fk_tasks_patient_id"))
    private Patient patient;

    @NotNull(message = "全員共通タスクの有無を入力してください")
    @Column(name = "assigned_to_all")
    private boolean assignedToAll;

    @NotNull(message = "優先度を入力してください")
    private Priority priority;

    @NotNull(message = "ステータスを入力してください")
    private TaskStatus taskStatus;

    private LocalDateTime dueDate;

    @ManyToMany
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> assignees;
}
