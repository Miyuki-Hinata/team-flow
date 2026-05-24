package com.example.teamflow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.example.teamflow.enums.Role;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "ログインIDを入力してください")
    @Column(name = "login_id", unique = true)
    private String loginId;

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

    @NotEmpty(message = "メールアドレスを入力してください")
    private String email;

    @JsonIgnore
    @NotEmpty(message = "パスワードを入力してください")
    private String password;

    @ManyToOne
    @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_users_department_id"))
    private Department department;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    private Long level;
}
