package com.example.teamflow.dto;

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

    @NotEmpty(message = "パスワードを入力してください")
    private String password;

    private Long departmentId;

    @NotNull(message = "権限レベルを入力してください")
    private Long level;

    private Long updatedBy;
}
