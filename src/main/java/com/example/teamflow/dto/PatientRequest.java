package com.example.teamflow.dto;

import com.example.teamflow.enums.Sex;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PatientRequest {
    @NotEmpty(message = "苗字を入力してください")
    private String lastName;

    @NotEmpty(message = "名前を入力してください")
    private String firstName;

    @NotEmpty(message = "苗字のかなを入力してください")
    private String lastNameKana;

    @NotEmpty(message = "名前のかなを入力してください")
    private String firstNameKana;

    @NotNull(message = "生年月日を入力してください")
    private LocalDate birth;

    @NotNull(message = "性別を入力してください")
    private Sex sex;

    // 住所は任意（緊急入院時に不明な場合がある）
    private String address;

    private String tel;

    // 緊急連絡先の人物名は任意（後から追加できるようにする）
    private String emergencyContactName;

    // 緊急連絡先の電話番号は任意（同上）
    private String emergencyContactTel;

    private Long doctorId;
    private Long departmentId;
    private Long updatedBy;
}
