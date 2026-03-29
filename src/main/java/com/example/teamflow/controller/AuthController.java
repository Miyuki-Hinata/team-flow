package com.example.teamflow.controller;

import com.example.teamflow.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String loginId = request.get("loginId");
        String password = request.get("password");

        // ログインIDとパスワードを検証
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginId, password)
        );

        // JWT生成して返却
        String token = jwtUtil.generateToken(loginId);
        return Map.of("token", token);
    }
}
