package com.example.teamflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
// (Spring Boot 4.x）
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// demo … デモデータ(data-demo.sql)を投入する
// test … application-test.properties（テスト専用の jwt.secret）を読み込む
//
// このクラスはトークンの「ライフサイクル」を検証する。
// README の設計ポイント（リフレッシュトークンのローテーション・DB失効・typ クレームによる
// 用途外利用の拒否・論理削除ユーザーの認証拒否）は、ここのテストが裏付けになる。
@ActiveProfiles({"demo", "test"})
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    // デモデータ(data-demo.sql)の全ユーザーに共通のパスワード（DepartmentControllerTest と同じ方針）
    private static final String DEMO_PASSWORD = "admin1234";

    // AuthController がリフレッシュトークンを載せる Cookie 名
    private static final String REFRESH_COOKIE = "refreshToken";

    @Autowired
    private MockMvc mockMvc;

    /**
     * ログインしてレスポンス全体（MvcResult）を返す。
     *
     * <p>DepartmentControllerTest の loginAndGetToken と違い MvcResult を返すのは、
     * このクラスではアクセストークン（ボディ）だけでなく
     * リフレッシュトークン（Set-Cookie）も取り出す必要があるため。
     * ログインが 200 であることを先に検証するのは同じ（NPE での原因不明な失敗を防ぐ）。
     */
    private MvcResult login(String loginId) throws Exception {
        return mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"loginId\": \"" + loginId + "\", \"password\": \"" + DEMO_PASSWORD + "\"}")
                )
                .andExpect(status().isOk())
                .andReturn();
    }

    // レスポンスボディからアクセストークンを取り出す
    private String extractAccessToken(MvcResult result) throws Exception {
        return new ObjectMapper()
                .readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
    }

    // Set-Cookie からリフレッシュトークンの Cookie を取り出す
    private Cookie extractRefreshCookie(MvcResult result) {
        Cookie cookie = result.getResponse().getCookie(REFRESH_COOKIE);
        if (cookie == null) {
            // Cookie が無いままテストを進めると NPE になり原因が分かりにくいので、ここで明示的に落とす
            throw new IllegalStateException("レスポンスに " + REFRESH_COOKIE + " Cookie がありません");
        }
        return cookie;
    }

    @Test
    void リフレッシュトークンをAPI認証に使うと401になる() throws Exception {
        String refreshToken = extractRefreshCookie(login("general")).getValue();

        // 寿命7日のリフレッシュトークンを Authorization ヘッダに入れても API は通らないこと。
        // これが通ると、アクセストークンを15分に短くした意味が失われる。
        // JwtAuthFilter が typ クレーム（access/refresh）を検証していることの裏付け。
        mockMvc.perform(
                        get("/api/departments")
                                .header("Authorization", "Bearer " + refreshToken)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void Cookieなしではリフレッシュできない() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 使用済みのリフレッシュトークンではリフレッシュできない() throws Exception {
        Cookie oldCookie = extractRefreshCookie(login("general"));

        // 1回目のリフレッシュは成功し、新しいリフレッシュトークンが発行される（ローテーション）
        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh").cookie(oldCookie))
                .andExpect(status().isOk())
                .andReturn();
        Cookie newCookie = extractRefreshCookie(refreshed);

        // 同じ（使用済みの）トークンをもう一度使うと拒否される。
        // JWT としてはまだ有効期限内なので、これが 401 になるのは
        // DB 側の revoked フラグを見ている証拠（＝盗まれたトークンの再利用を防げる）。
        mockMvc.perform(post("/api/auth/refresh").cookie(oldCookie))
                .andExpect(status().isUnauthorized());

        // ローテーションで発行された新しいトークンは使える
        mockMvc.perform(post("/api/auth/refresh").cookie(newCookie))
                .andExpect(status().isOk());
    }

    @Test
    void ログアウトするとリフレッシュトークンが失効する() throws Exception {
        Cookie cookie = extractRefreshCookie(login("general"));

        mockMvc.perform(post("/api/auth/logout").cookie(cookie))
                .andExpect(status().isOk());

        // ログアウトは Cookie 削除だけでなく DB 上の失効も行うため、
        // Cookie の値を保持し続けても（＝万一盗まれていても）再利用できない
        mockMvc.perform(post("/api/auth/refresh").cookie(cookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 論理削除されたユーザーのアクセストークンは拒否される() throws Exception {
        // 'msw'（id=23）を削除対象にする。他のテストが使わないユーザーを選んでいる。
        // デモデータは起動のたびにリセット→再投入されるので、ここで消しても次回起動で復元される。
        String mswToken = extractAccessToken(login("msw"));
        String adminToken = extractAccessToken(login("admin"));

        // 管理者が msw を論理削除する
        mockMvc.perform(
                        delete("/api/users/23")
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isOk());

        // 削除前に発行されたトークンは、JWT の有効期限内（15分以内）でも拒否されること。
        // UserDetailsServiceImpl が deleted_at IS NULL で引いているため、
        // 毎リクエストの認証時に削除済みアカウントを弾ける。
        mockMvc.perform(
                        get("/api/departments")
                                .header("Authorization", "Bearer " + mswToken)
                )
                .andExpect(status().isUnauthorized());
    }
}
