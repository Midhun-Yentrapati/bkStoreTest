package com.bookstore.user_authentication_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for TestController
 * Tests health check and diagnostic endpoints
 */
@WebMvcTest(TestController.class)
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHealthCheck_Success() throws Exception {
        mockMvc.perform(get("/api/test/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("User Authentication Service"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testDetailedHealthCheck_Success() throws Exception {
        mockMvc.perform(get("/api/test/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("User Authentication Service"))
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.uptime").exists())
                .andExpect(jsonPath("$.database.status").exists())
                .andExpect(jsonPath("$.memory").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testServiceInfo_Success() throws Exception {
        mockMvc.perform(get("/api/test/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("User Authentication Service"))
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.port").exists())
                .andExpect(jsonPath("$.profiles").exists())
                .andExpect(jsonPath("$.buildTime").exists());
    }

    @Test
    void testDatabaseConnectivity_Success() throws Exception {
        mockMvc.perform(get("/api/test/database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.database.connected").value(true))
                .andExpect(jsonPath("$.database.type").exists())
                .andExpect(jsonPath("$.database.url").exists())
                .andExpect(jsonPath("$.database.driver").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testCorsHeaders_Success() throws Exception {
        mockMvc.perform(options("/api/test/health")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"))
                .andExpect(header().string("Access-Control-Allow-Headers", "*"));
    }

    @Test
    void testHealthCheck_ContentType() throws Exception {
        mockMvc.perform(get("/api/test/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testInvalidEndpoint_NotFound() throws Exception {
        mockMvc.perform(get("/api/test/invalid"))
                .andExpect(status().isNotFound());
    }
}
