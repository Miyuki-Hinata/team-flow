package com.example.teamflow.entity;

import com.example.teamflow.enums.Sex;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
public class Patient extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "苗字を入力してください")
    @Column(name = "last_name")
    private String lastName;

    @NotEmpty(message = "名前を入力してください")
    @Column(name = "first_name")
    private String firstName;

    @NotEmpty(message = "苗字のかなを入力してください")
    @Column(name = "last_name_kana")
    private String lastNameKana;

    @NotEmpty(message = "名前のかなを入力してください")
    @Column(name = "first_name_kana")
    private String firstNameKana;

    @NotNull(message = "生年月日を入力してください")
    private LocalDate birth;

    @NotNull(message = "性別を入力してください")
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @NotEmpty(message = "住所を入力してください")
    private String address;

    private String tel;

    @NotEmpty(message = "緊急連絡先の人物名を入力してください")
    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @NotEmpty(message = "緊急連絡先の電話番号を入力してください")
    @Column(name = "emergency_contact_tel")
    private String emergencyContactTel;

    @ManyToOne
    @JoinColumn(name = "doctor_id", foreignKey = @ForeignKey(name = "fk_patients_doctor_id"))
    private User doctor;

    @ManyToOne
    @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_patients_department_id"))
    private Department department;
}
