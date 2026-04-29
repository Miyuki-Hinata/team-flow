package com.example.teamflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "announcement_reads")
@Getter
@Setter
public class AnnouncementRead extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "announcement_id", foreignKey = @ForeignKey(name ="fk_announcement_reads_announcement_id"))
    private Announcement announcement;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name ="fk_announcement_reads_user_id"))
    private User user;
}
