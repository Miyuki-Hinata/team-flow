package com.example.teamflow.dto;

import com.example.teamflow.enums.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    @NotEmpty(message = "ログインIDを入力してください")
    private String loginId;

    @NotEmpty(message = "苗字を入力してください")
    private String lastName;

    @NotEmpty(message = "名前を入力してください")
    private String firstName;

    @NotEmpty(message = "苗字のかなを入力してください")
    private String lastNameKana;

    @NotEmpty(message = "名前のかなを入力してください")
    private String firstNameKana;

    @NotEmpty(message = "メールアドレスを入力してください")
    private String email;

    // パスワードは「新規作成時は必須／編集時は空欄なら変更しない」という扱いにしたいので、
    // ここでは @NotEmpty を付けない（DTO の検証は作成・編集で共通に効いてしまうため）。
    // 新規作成時の必須チェックは UserService.createUser で行う。
    private String password;

    private Long departmentId;

    // 職種（医師・看護師など）。User エンティティ側が @NotNull なので、
    // リクエストでも必須にして「職種なしのユーザーが作られる」のを入口で防ぐ。
    @NotNull(message = "職種を選択してください")
    private Role role;

    // 権限レベル。1 = 一般/ 2 = 管理者（User.isAdmin() が level == 2 で判定する）。
    @NotNull(message = "権限レベルを入力してください")
    private Long level;

    private Long updatedBy;
}
