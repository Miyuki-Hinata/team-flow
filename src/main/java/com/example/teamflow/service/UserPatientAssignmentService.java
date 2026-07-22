package com.example.teamflow.service;

import com.example.teamflow.entity.Patient;
import com.example.teamflow.entity.User;
import com.example.teamflow.entity.UserPatientAssignment;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.PatientRepository;
import com.example.teamflow.repository.UserPatientAssignmentRepository;
import com.example.teamflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserPatientAssignmentService {
    @Autowired
    private UserPatientAssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    /**
     * 受け持ち患者の一覧を返す。
     * 中間テーブルの行から患者エンティティだけを取り出す（フロントは Patient[] をそのまま扱える）。
     */
    public List<Patient> getAssignedPatients(Long userId) {
        return assignmentRepository.findByUserId(userId).stream()
                .map(UserPatientAssignment::getPatient)
                .toList();
    }

    /**
     * 受け持ちを「集合置換」で更新する。
     *
     * 送られた patientIds がそのまま最終状態になるよう、
     *   1) 既存の割り当てを全削除
     *   2) 新しい集合を全挿入
     * という単純な入れ替えにしている。差分計算をしないぶん実装が単純で、同じ集合を何度送っても
     * 結果が変わらない（冪等）ため、リトライや二重送信に強い。
     */
    @Transactional
    public void replaceAssignments(Long userId, List<Long> patientIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));

        // 1) このユーザーの既存割り当てを全削除（置換のための初期化）
        assignmentRepository.deleteByUserId(userId);

        // ここで明示的に flush して DELETE を先に DB へ送る。
        // Hibernate は 1 回の flush 内で「INSERT → DELETE」の順に SQL を並べるため、
        // flush しないと「元も新も含む患者（＝据え置きの患者）」の INSERT が旧行の DELETE より先に走り、
        // ユニーク制約 (user_id, patient_id) 違反になる。DELETE を先出しして衝突を防ぐ。
        assignmentRepository.flush();

        // 空配列＝全解除。削除だけで完了
        if (patientIds == null || patientIds.isEmpty()) {
            return;
        }

        // 同じ患者IDが重複して来てもユニーク制約違反にしないよう、あらかじめ重複を除去する
        List<Long> distinctIds = patientIds.stream().distinct().toList();

        // 2) 新しい集合を挿入。存在しない患者IDが混ざっていたら 404 で弾く
        for (Long patientId : distinctIds) {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new ResourceNotFoundException("患者が見つかりません id: " + patientId));

            UserPatientAssignment assignment = new UserPatientAssignment();
            assignment.setUser(user);
            assignment.setPatient(patient);
            assignmentRepository.save(assignment);
        }
    }
}
