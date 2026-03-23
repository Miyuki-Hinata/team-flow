package com.example.teamflow.controller;

import com.example.teamflow.entity.User;
import com.example.teamflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/api/users")
    public List<User> getUser() {
        return userService.getUser();
    }

    @GetMapping("/api/users/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/api/users")
    public User createUser(
            @Valid @RequestBody User user
    ) {
        return userService.createUser(user);
    }

    @PutMapping("/api/users/{id}")
    public User updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user
    ) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/api/users/{id}")
    public String deleteUser(
            @PathVariable Long id
    ) {
        return userService.deleteUser(id);
    }
}


//GET    /api/users          → 一般ユーザーOK
//GET    /api/users/{id}     → 一般ユーザーOK
//POST   /api/users          → 管理者のみ
//PUT /api/users/{id}
//        - 自分のIDと一致する場合 → 一般ユーザーOK
//    - 他人のIDの場合 → 管理者のみ
//DELETE /api/users/{id}     → 管理者のみ