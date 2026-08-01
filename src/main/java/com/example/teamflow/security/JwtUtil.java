package com.example.teamflow.security;

import io.jsonwebtoken.*;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JwtUtil {

    // 署名アルゴリズムを「コードで」固定する。
    // 明示しない場合、jjwt は鍵の長さを見て HS256/HS384/HS512 を自動選択するため、
    // 環境ごとに JWT_SECRET の長さが違うと署名アルゴリズムまで変わってしまう。
    // 暗号アルゴリズムの選択は運用設定ではなく実装の決定事項なので、ここで固定する。
    // HS256 を選ぶ理由：JWT で最も広く使われ互換性が高く、256bit HMAC の強度で十分なため。
    private static final SignatureAlgorithm ALGORITHM = SignatureAlgorithm.HS256;

    // 上記アルゴリズムが要求する最小の鍵長（RFC 7518）。
    // 定数を直書きせずアルゴリズムから導出することで、ALGORITHM を変えれば検証条件も自動で追従する。
    // getMinKeyLength() はビット単位で返るのでバイトに直す（HS256 なら 256bit = 32バイト）。
    private static final int MIN_SECRET_BYTES = ALGORITHM.getMinKeyLength() / 8;

    // 環境変数から読み込む
    @Value("${jwt.secret}")
    private String secret;
    // アクセストークンの有効期限（15分）
    private static final long EXPIRATION = 1000L * 60 * 15;
    // リフレッシュトークンの有効期限（7日）
    private static final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 7;

    // 署名鍵。@PostConstruct で一度だけ生成し、以降は使い回す。
    private Key signingKey;

    /**
     * 起動時に秘密鍵を検証し、署名鍵を組み立てる。
     *
     * ここで落とすのが重要な理由：この検証が無いと、鍵が短すぎても起動自体は成功してしまい、
     * 最初のログイン要求で署名時に WeakKeyException が投げられて初めて気づくことになる。
     * 設定ミスは「起動した／しない」で判定できる方が運用上わかりやすい。
     */
    @PostConstruct
    void initSigningKey() {
        // 文字コードを明示する。既定の文字コードに任せると、実行環境（コンテナ等）で
        // 既定が変わったときに同じ文字列から別のバイト列＝別の鍵が導出され、
        // 発行済みトークンが一斉に検証できなくなる。
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret が短すぎます（" + keyBytes.length + " バイト）。"
                            + ALGORITHM.getValue() + " には " + MIN_SECRET_BYTES + " バイト以上が必要です。"
                            + "環境変数 JWT_SECRET に十分な長さのランダム文字列を設定してください。"
                            + "生成例: openssl rand -base64 48");
        }

        // Keys.hmacShaKeyFor() は鍵の長さから HmacSHA256/384/512 を「自動で」選んで鍵に名前を付けるため、
        // ここでも設定値の長さがアルゴリズム選択に影響してしまう。
        // ALGORITHM で決めた方式を鍵にも明示的に与え、選択箇所を ALGORITHM の一箇所に集約する。
        this.signingKey = new SecretKeySpec(keyBytes, ALGORITHM.getJcaName());
    }

    // 署名・検証で使う鍵を返す
    private Key getSigningKey() {
        return signingKey;
    }

    // JWTを生成する
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey(), ALGORITHM)
                .compact();
    }

    // リフレッシュトークン（寿命の長いJWT）を生成する
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setId(java.util.UUID.randomUUID().toString()) // jtiクレーム：トークンを一意にするためのランダムID
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(getSigningKey(), ALGORITHM)
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