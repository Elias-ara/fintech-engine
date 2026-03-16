package com.fintechengine.modules.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintechengine.modules.account.entity.Account;
import com.fintechengine.modules.account.repository.AccountRepository;
import com.fintechengine.modules.auth.dto.LoginRequest;
import com.fintechengine.modules.auth.dto.RegisterRequest;
import com.fintechengine.modules.transaction.dto.DepositRequest;
import com.fintechengine.modules.transaction.dto.WithdrawalRequest;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String token;
    private UUID accountId;

    @BeforeEach
    void setUp() throws Exception {
        // Cria usuário direto no banco
        User user = new User("João", "12345678901", "joao@email.com", passwordEncoder.encode("senha123"));
        userRepository.save(user);

        // Cria conta direto no banco
        Account account = new Account(user);
        accountRepository.save(account);
        accountId = account.getId();

        // Obtém token via login
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("joao@email.com", "senha123")
                ))).andReturn();

        token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test
    void deposit_shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("100.00"), "dep-001")
                )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deposit_shouldIncreaseBalance() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("200.00"), "dep-001")
                )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.operationType").value("DEPOSIT"));

        mockMvc.perform(get("/accounts/" + accountId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(200.00));
    }

    @Test
    void withdrawal_shouldReturn422WhenInsufficientBalance() throws Exception {
        mockMvc.perform(post("/transactions/withdrawal")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new WithdrawalRequest(accountId, new BigDecimal("500.00"), "saq-001")
                )))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Insufficient balance"));
    }

    @Test
    void deposit_shouldBeIdempotent() throws Exception {
        var request = new DepositRequest(accountId, new BigDecimal("100.00"), "dep-idem");

        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Segunda chamada com mesma idempotency key — não duplica
        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Saldo deve ser 100, não 200
        mockMvc.perform(get("/accounts/" + accountId)
                .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.balance").value(100.00));
    }
}
