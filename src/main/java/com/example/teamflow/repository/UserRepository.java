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

    // 論理削除（deleted_at がセット済み）のユーザーを除いた一覧。
    // 削除したユーザーが管理画面の一覧や担当者の選択肢に残り続けないようにする。
    List<User> findByDeletedAtIsNull();
    List<User> findByRoleAndDeletedAtIsNull(Role role);
}
