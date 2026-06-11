package com.example.teamflow.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {
    @NotEmpty(message = "現在のパスワードを入力してください")
    private String currentPassword;

    @NotEmpty(message = "新しいパスワードを入力してください")
    @Size(min = 8, message = "新しいパスワードは8文字以上で入力してください")
    private String newPassword;
}
