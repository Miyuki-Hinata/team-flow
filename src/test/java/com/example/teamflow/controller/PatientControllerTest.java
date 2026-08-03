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

// 患者の登録・削除が管理者に限定されていることを検証する。
// 更新(PUT)は現場での情報更新を想定して一般ユーザーにも許可しているため、
// その差（登録・削除だけが管理者限定）が守られていることを確認する。
@ActiveProfiles({"demo", "test"})
@SpringBootTest
@AutoConfigureMockMvc
public class PatientControllerTest {

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

    // ボディが空の {} なのは意図的（UserControllerTest と同じ理由：
    // この 403 は SecurityConfig の URL ルールが返すもので、ボディの中身は関心外）。

    @Test
    void 一般ユーザーは患者を登録できない() throws Exception {
        mockMvc.perform(
                        post("/api/patients")
                                .header("Authorization", "Bearer " + generalToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void 一般ユーザーは患者を削除できない() throws Exception {
        mockMvc.perform(
                        delete("/api/patients/1")
                                .header("Authorization", "Bearer " + generalToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void 一般ユーザーも患者を参照できる() throws Exception {
        // 登録・削除と違い、参照は全職種の日常業務なので一般ユーザーでも通ること
        mockMvc.perform(
                        get("/api/patients")
                                .header("Authorization", "Bearer " + generalToken)
                )
                .andExpect(status().isOk());
    }
}
