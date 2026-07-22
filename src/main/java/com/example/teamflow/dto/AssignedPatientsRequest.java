package com.example.teamflow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 受け持ち患者を更新するリクエスト。
 *
 * patientIds は「差分」ではなく「最終状態そのもの」。この配列がそのまま受け持ちの集合になる（集合置換）。
 * ・空配列 []          → 全解除（クリアボタン）
 * ・[1, 5, 8]          → 患者 1・5・8 だけを受け持ちにする
 * 同じ配列を何度送っても結果は変わらない（冪等）。
 */
@Getter
@Setter
public class AssignedPatientsRequest {
    // null は不正入力として弾く。ただし空配列（全解除）は正当なので @NotEmpty ではなく @NotNull を使う。
    @NotNull(message = "patientIds は必須です（全解除は空配列を送ってください）")
    private List<Long> patientIds;
}
