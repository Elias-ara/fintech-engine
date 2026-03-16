package com.fintechengine.modules.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintechengine.modules.auth.dto.LoginRequest;
import com.fintechengine.modules.user.entity.User;
import com.fintechengine.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String token;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        User user = new User("João", "12345678901", "joao@email.com", passwordEncoder.encode("senha123"));
        userRepository.save(user);
        userId = user.getId();

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("joao@email.com", "senha123")
                ))).andReturn();

        token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test
    void findById_shouldReturnUser() throws Exception {
        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("João"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    void findById_shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/users/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isUnauthorized());
    }
}
