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

import static com.example.teamflow.enums.Priority.MEDIUM;
import static com.example.teamflow.enums.TaskStatus.CREATED;

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

    @Column(name = "assigned_to_all")
    private boolean assignedToAll = false;

    @NotNull(message = "優先度を入力してください")
    @Enumerated(EnumType.STRING)
    private Priority priority = MEDIUM;

    @NotNull(message = "ステータスを入力してください")
    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus = CREATED;

    private LocalDateTime dueDate;

    @ManyToMany
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> assignees;

    @ManyToMany
    @JoinTable(
            name ="related_tasks",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "related_task_id")
    )
    private List<Task> relatedTasks;

}
