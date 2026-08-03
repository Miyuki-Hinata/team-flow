package com.example.teamflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
// (Spring Boot 4.x）
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// ユーザー（職員）管理が管理者に限定されていることを検証する。
// UI 側でも管理メニューを出し分けているが、防御の本体は API 側（SecurityConfig）であり、
// curl 等で直接叩かれても弾けることをここで担保する。
@ActiveProfiles({"demo", "test"})
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    // デモデータ(data-demo.sql)の全ユーザーに共通のパスワード（DepartmentControllerTest と同じ方針）
    private static final String DEMO_PASSWORD = "admin1234";

    @Autowired
    private MockMvc mockMvc;

    private String generalToken;

    @BeforeEach
    void setUp() throws Exception {
        generalToken = loginAndGetToken("general");  // level=1 → ROLE_USER
    }

    // ログインしてアクセストークンを取り出す（先に 200 を検証するのは DepartmentControllerTest と同じ理由）
    private String loginAndGetToken(String loginId) throws Exception {
        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"loginId\": \"" + loginId + "\", \"password\": \"" + DEMO_PASSWORD + "\"}")
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return new ObjectMapper()
                .readTree(response)
                .get("token")
                .asText();
    }

    // ボディが空の {} なのは意図的。この 403 は SecurityConfig の URL ルールが
    // コントローラより手前で返すものなので、ボディの中身はテストの関心ではない
    // （中身まで作り込むと「バリデーションのテスト」と誤読されるため、あえて空にしている）。

    @Test
    void 一般ユーザーはユーザーを作成できない() throws Exception {
        mockMvc.perform(
                        post("/api/users")
                                .header("Authorization", "Bearer " + generalToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void 一般ユーザーは他のユーザーを更新できない() throws Exception {
        mockMvc.perform(
                        put("/api/users/2")
                                .header("Authorization", "Bearer " + generalToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void 一般ユーザーはユーザーを削除できない() throws Exception {
        mockMvc.perform(
                        delete("/api/users/2")
                                .header("Authorization", "Bearer " + generalToken)
                )
                .andExpect(status().isForbidden());
    }
}
