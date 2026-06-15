package com.example.teamflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_summaries")
@Getter
@Setter
@NoArgsConstructor
public class TaskSummary extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", unique = true, foreignKey = @ForeignKey(name = "fk_task_summary_patient_id"))
    private Patient patient;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @ManyToOne
    @JoinColumn(name = "generated_by_user_id", foreignKey = @ForeignKey(name = "fk_task_summary_user_id"))
    private User generatedBy;
}