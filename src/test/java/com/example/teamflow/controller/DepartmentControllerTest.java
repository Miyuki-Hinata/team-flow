package com.example.teamflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
// (Spring Boot 4.x）
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// demo … デモデータ(data-demo.sql)を投入する（このテストはログインに admin ユーザーを使う）
// test … application-test.properties（テスト専用の jwt.secret）を読み込む
@ActiveProfiles({"demo", "test"})
@SpringBootTest
@AutoConfigureMockMvc
public class DepartmentControllerTest {

    // デモデータ(data-demo.sql)の全ユーザーに共通のパスワード。
    // ここを定数にしているのは、シード側のパスワードが変わったときに
    // 直す箇所を1つにするため（以前は各テストに直書きされていて、
    // 実際にシードと食い違ったまま気づけず失敗していた）。
    private static final String DEMO_PASSWORD = "admin1234";

    @Autowired
    private MockMvc mockMvc;

    private String adminToken;
    private String generalToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = loginAndGetToken("admin");      // level=2 → ROLE_ADMIN
        generalToken = loginAndGetToken("general");  // level=1 → ROLE_USER
    }

    /**
     * ログインしてアクセストークンを取り出す。
     *
     * <p>ログインが 200 であることを先に検証しているのが要点。
     * これが無いと、ログインに失敗しても処理が進んでしまい、
     * レスポンスに token が無いために {@code NullPointerException} で落ちる。
     * 「NPE」だけ見てもパスワード違いだとは分からず、原因追跡に時間がかかる。
     * 先に status を検証しておけば「401 が返っている」と表示され、即座に切り分けできる。
     */
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

    @Test
    void 管理者は部署を作成できる() throws Exception {
        mockMvc.perform(
                        post("/api/departments")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"departmentName\": \"テスト部署\"}")
                )
                .andExpect(status().isCreated()); // 201
    }

    @Test
    void 一般ユーザーは部署を作成できない() throws Exception {
        mockMvc.perform(
                post("/api/departments")
                        .header("Authorization", "Bearer " + generalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departmentName\": \"テスト部署2\"}")

        )
                .andExpect(status().isForbidden());
    }

    @Test
    void 管理者は部署を取得できる() throws Exception {
        mockMvc.perform(
                get("/api/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk());
    }

    @Test
    void 一般ユーザーは部署を取得できる() throws Exception {
        mockMvc.perform(
                        get("/api/departments")
                                .header("Authorization", "Bearer " + generalToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }













}