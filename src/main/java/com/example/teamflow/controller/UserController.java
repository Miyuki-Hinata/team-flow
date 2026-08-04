package com.example.teamflow.controller;

import com.example.teamflow.dto.PasswordChangeRequest;
import com.example.teamflow.dto.UserRequest;
import com.example.teamflow.dto.UserResponse;
import com.example.teamflow.entity.User;
import com.example.teamflow.enums.Role;
import com.example.teamflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/api/users")
    public List<User> getUsers(@RequestParam(required = false) Role role) {
        if (role == null) {
            return userService.getUsers();
        } else {
            return userService.getUsersByRole(role);
        }
    }

    @GetMapping("/api/users/me")
    public UserResponse getCurrentUser() {
        String loginId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userService.getUserByLoginId(loginId);
        return UserResponse.from(user);
    }

    @PutMapping("/api/users/me/password")
    public Map<String, String> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(request);
        return Map.of("message", "パスワードを変更しました");
    }

    @GetMapping("/api/users/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/api/users")
    public User createUser(
            @Valid @RequestBody UserRequest request
    ) {
        return userService.createUser(request);
    }

    @PutMapping("/api/users/{id}")
    public User updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request
    ) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/api/users/{id}")
    public String deleteUser(
            @PathVariable Long id
    ) {
        return userService.deleteUser(id);
    }
}


// 権限ルール（実装は SecurityConfig の URL ルール。ここは早見表）
//GET    /api/users             → 一般ユーザーOK
//GET    /api/users/{id}        → 一般ユーザーOK
//POST   /api/users             → 管理者のみ
//PUT    /api/users/{id}        → 管理者のみ
//PUT    /api/users/me/password → 一般ユーザーOK（自分のパスワード変更のみ）
//DELETE /api/users/{id}        → 管理者のみ（論理削除）
// ※「自分のIDへの PUT は一般ユーザーOK」という案は未実装（必要になった時点で判断）
