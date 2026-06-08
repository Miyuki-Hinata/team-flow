package com.example.teamflow.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JwtUtil {

    // 環境変数から読み込む
    @Value("${jwt.secret}")
    private String secret;
    // アクセストークンの有効期限（15分）
    private static final long EXPIRATION = 1000L * 60 * 15;
    // リフレッシュトークンの有効期限（7日）
    private static final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 7;

    // 秘密鍵をKeyオブジェクトに変換
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // JWTを生成する
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    // リフレッシュトークン（寿命の長いJWT）を生成する
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setId(java.util.UUID.randomUUID().toString()) // jtiクレーム：トークンを一意にするためのランダムID
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    // リフレッシュトークンの有効期限（DB保存用）を計算する
    public LocalDateTime getRefreshTokenExpiry() {
        return LocalDateTime.now().plus(Duration.ofMillis(REFRESH_EXPIRATION));
    }

    // JWTからユーザー名を取り出す
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // JWTが有効かどうか検証する
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}