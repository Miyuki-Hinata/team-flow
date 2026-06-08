package com.example.teamflow.controller;

import com.example.teamflow.entity.RefreshToken;
import com.example.teamflow.entity.User;
import com.example.teamflow.repository.RefreshTokenRepository;
import com.example.teamflow.security.JwtUtil;
import com.example.teamflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String loginId = request.get("loginId");
        String password = request.get("password");

        // ログインIDとパスワードを検証
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginId, password)
        );

        User user = userService.getUserByLoginId(loginId);

        // アクセストークン（短命・レスポンスボディで返す）とリフレッシュトークン（長命・Cookieで返す）を発行
        String accessToken = jwtUtil.generateToken(loginId);
        String refreshToken = jwtUtil.generateRefreshToken(loginId);

        // 同じユーザーの古いリフレッシュトークンは破棄し、新しいものをDBに保存（多重ログイン時の整理のため）
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setExpiresAt(jwtUtil.getRefreshTokenExpiry());
        refreshTokenRepository.save(refreshTokenEntity);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(refreshToken).toString())
                .body(Map.of("token", accessToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {

        // Cookieが無い、またはJWTとして不正・期限切れなら認証エラー
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<RefreshToken> stored = refreshTokenRepository.findByToken(refreshToken);

        // DBに登録されていない、すでに失効済み、DB上の有効期限切れなら認証エラー
        if (stored.isEmpty() || stored.get().isRevoked()
                || stored.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RefreshToken oldRefreshTokenEntity = stored.get();
        User user = oldRefreshTokenEntity.getUser();

        // ローテーション：使用済みの古いリフレッシュトークンは失効させる
        oldRefreshTokenEntity.setRevoked(true);
        refreshTokenRepository.save(oldRefreshTokenEntity);

        // 新しいアクセストークンとリフレッシュトークンを発行してDBに保存
        String newAccessToken = jwtUtil.generateToken(user.getLoginId());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getLoginId());

        RefreshToken newRefreshTokenEntity = new RefreshToken();
        newRefreshTokenEntity.setToken(newRefreshToken);
        newRefreshTokenEntity.setUser(user);
        newRefreshTokenEntity.setExpiresAt(jwtUtil.getRefreshTokenExpiry());
        refreshTokenRepository.save(newRefreshTokenEntity);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(newRefreshToken).toString())
                .body(Map.of("token", newAccessToken));
    }

    // リフレッシュトークンをHttpOnly CookieとしてセットするためのResponseCookieを組み立てる
    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();
    }
}
