package com.example.teamflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 受け持ち患者の割り当て（ユーザー ↔ 患者 の多対多を表す中間テーブル）。
 *
 * 「受け持つ患者」を選ぶと 1 行できる。クリア/再選択するまで維持される
 * （standing assignment：特定の日に紐づけない。将来シフト履歴が必要になったら assigned_date を足す）。
 *
 * 実装方針は既存の {@link AnnouncementRead}（ユーザー ↔ お知らせ の中間テーブル）に揃えた。
 * 複合主キーではなくサロゲート id + ユニーク制約とすることで、JPA のマッピングを単純に保ちつつ
 * 「1 ユーザーにつき同じ患者は 1 行だけ」を DB 側で保証する。
 */
@Entity
@Table(
        name = "user_patient_assignments",
        // 並行 PUT や React StrictMode の二重発火で同じ (user, patient) が重複 INSERT されるのを
        // DB のユニーク制約で確実に弾く（AnnouncementRead と同じ考え方）
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_patient_assignments_user_patient",
                columnNames = {"user_id", "patient_id"}
        )
)
@Getter
@Setter
public class UserPatientAssignment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 受け持つ側のユーザー（＝ログイン中の担当スタッフ）
    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_patient_assignments_user_id"))
    private User user;

    // 受け持たれる側の患者
    @ManyToOne
    @JoinColumn(name = "patient_id", foreignKey = @ForeignKey(name = "fk_user_patient_assignments_patient_id"))
    private Patient patient;
}
