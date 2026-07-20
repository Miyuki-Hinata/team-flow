package com.example.teamflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "announcement_reads",
        // 1 ユーザーにつき 1 お知らせは 1 レコード。並行 INSERT や React StrictMode の
        // useEffect 二重発火で重複行が入るのを DB 側で確実に防ぐ
        uniqueConstraints = @UniqueConstraint(
                name = "uq_announcement_reads_announcement_user",
                columnNames = {"announcement_id", "user_id"}
        )
)
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
