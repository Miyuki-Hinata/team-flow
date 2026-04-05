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
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String adminToken;
    private String generalToken;

    @BeforeEach
    void setUp() throws Exception {
        // 管理者トークン取得
        String adminResponse = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"loginId\": \"admin\", \"password\": \"password123\"}")
                )
                .andReturn().getResponse().getContentAsString();

        adminToken = new ObjectMapper()
                .readTree(adminResponse)
                .get("token")
                .asText();

        // 一般ユーザートークン取得
        String generalResponse = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"loginId\": \"general\", \"password\": \"password123\"}")
                )
                .andReturn().getResponse().getContentAsString();

        generalToken = new ObjectMapper()
                .readTree(generalResponse)
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