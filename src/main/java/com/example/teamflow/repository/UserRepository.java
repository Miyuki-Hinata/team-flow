package com.example.teamflow.repository;

import com.example.teamflow.entity.User;
import com.example.teamflow.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);

    // 認証（ログイン時と、毎リクエストの JWT 検証時）で使う版。論理削除済みユーザーを除外する。
    // findByLoginId と分けている理由：
    //   認証は「このアカウントが今も有効か」を判定する境界であり、ここで削除済みを通すと
    //   退職者が削除後もログインでき API も叩けてしまう（管理画面の一覧からは消えるため、
    //   管理者は「削除できた」と誤認する）。
    //   一方、既存レコードの「最終更新者」表示などでは削除済みユーザーも引けた方が
    //   都合がよいので、無条件版も残しておく。
    Optional<User> findByLoginIdAndDeletedAtIsNull(String loginId);

    // 論理削除（deleted_at がセット済み）のユーザーを除いた一覧。
    // 削除したユーザーが管理画面の一覧や担当者の選択肢に残り続けないようにする。
    List<User> findByDeletedAtIsNull();
    List<User> findByRoleAndDeletedAtIsNull(Role role);
}
