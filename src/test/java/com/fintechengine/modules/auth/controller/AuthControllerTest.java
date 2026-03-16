package com.fintechengine.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintechengine.modules.auth.dto.LoginRequest;
import com.fintechengine.modules.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ── Register ────────────────────────────────────────────────────────────

    @Test
    void register_shouldReturnTokenWhenValidRequest() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("João", "12345678901", "joao@email.com", "senha123")
                )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void register_shouldReturn422WhenEmailAlreadyExists() throws Exception {
        var request = new RegisterRequest("João", "12345678901", "joao@email.com", "senha123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("João 2", "98765432100", "joao@email.com", "senha123")
                )))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void register_shouldReturn422WhenDocumentAlreadyExists() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("João", "12345678901", "joao@email.com", "senha123")
                )));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("Maria", "12345678901", "maria@email.com", "senha123")
                )))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Document already registered"));
    }

    @Test
    void register_shouldReturn400WhenEmailIsBlank() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("João", "12345678901", "", "senha123")
                )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400WhenPasswordTooShort() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("João", "12345678901", "joao@email.com", "abc")
                )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("", "12345678901", "joao@email.com", "senha123")
                )))
                .andExpect(status().isBadRequest());
    }

    // ── Login ───────────────────────────────────────────────────────────────

    @Test
    void login_shouldReturnTokenWithValidCredentials() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("João", "12345678901", "joao@email.com", "senha123")
                )));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("joao@email.com", "senha123")
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_shouldReturn401WithWrongPassword() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("João", "12345678901", "joao@email.com", "senha123")
                )));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("joao@email.com", "errada")
                )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturn401WithNonExistentEmail() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("naoexiste@email.com", "senha123")
                )))
                .andExpect(status().isUnauthorized());
    }
}
