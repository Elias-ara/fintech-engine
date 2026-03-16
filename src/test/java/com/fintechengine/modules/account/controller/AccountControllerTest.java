package com.fintechengine.modules.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintechengine.modules.account.entity.Account;
import com.fintechengine.modules.account.repository.AccountRepository;
import com.fintechengine.modules.auth.dto.LoginRequest;
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
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String token;
    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() throws Exception {
        User user = new User("João", "12345678901", "joao@email.com", passwordEncoder.encode("senha123"));
        userRepository.save(user);
        userId = user.getId();

        Account account = new Account(user);
        accountRepository.save(account);
        accountId = account.getId();

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("joao@email.com", "senha123")
                ))).andReturn();

        token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();
    }

    // ========== POST /accounts (create) ==========

    @Test
    void create_shouldReturn201WithNewAccount() throws Exception {
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("userId", userId.toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void create_shouldReturn404WhenUserDoesNotExist() throws Exception {
        UUID fakeUserId = UUID.randomUUID();

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("userId", fakeUserId.toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("userId", userId.toString()))))
                .andExpect(status().isUnauthorized());
    }

    // ========== GET /accounts/{id} ==========

    @Test
    void findById_shouldReturnAccountDetails() throws Exception {
        mockMvc.perform(get("/accounts/" + accountId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void findById_shouldReturn404WhenAccountNotFound() throws Exception {
        UUID fakeAccountId = UUID.randomUUID();

        mockMvc.perform(get("/accounts/" + fakeAccountId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ========== PATCH /accounts/{id}/status ==========

    @Test
    void updateStatus_shouldBlockAccount() throws Exception {
        mockMvc.perform(patch("/accounts/" + accountId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("status", "BLOCKED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void updateStatus_shouldUnblockAccount() throws Exception {
        mockMvc.perform(patch("/accounts/" + accountId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("status", "BLOCKED"))));

        mockMvc.perform(patch("/accounts/" + accountId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("status", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void updateStatus_shouldCloseAccount() throws Exception {
        mockMvc.perform(patch("/accounts/" + accountId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("status", "CLOSED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void updateStatus_shouldReturn422WhenChangingClosedAccount() throws Exception {
        mockMvc.perform(patch("/accounts/" + accountId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("status", "CLOSED"))));

        mockMvc.perform(patch("/accounts/" + accountId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("status", "ACTIVE"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Cannot change status of a closed account"));
    }

    @Test
    void updateStatus_shouldReturn404WhenAccountNotFound() throws Exception {
        UUID fakeAccountId = UUID.randomUUID();

        mockMvc.perform(patch("/accounts/" + fakeAccountId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("status", "BLOCKED"))))
                .andExpect(status().isNotFound());
    }

    // ========== GET /accounts/{id}/transactions ==========

    @Test
    void getTransactions_shouldReturnPagedResults() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("100.00"), "dep-page-1")
                )));

        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("50.00"), "dep-page-2")
                )));

        mockMvc.perform(get("/accounts/" + accountId + "/transactions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getTransactions_shouldReturnEmptyPageWhenNoTransactions() throws Exception {
        mockMvc.perform(get("/accounts/" + accountId + "/transactions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ========== GET /accounts/{id}/ledger ==========

    @Test
    void getLedger_shouldReturnPagedLedgerEntries() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("100.00"), "dep-ledger-1")
                )));

        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("50.00"), "dep-ledger-2")
                )));

        mockMvc.perform(get("/accounts/" + accountId + "/ledger")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getLedger_shouldReturnEmptyPageWhenNoTransactions() throws Exception {
        mockMvc.perform(get("/accounts/" + accountId + "/ledger")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ========== Integration with transactions ==========

    @Test
    void deposit_shouldReturn422WhenAccountIsBlocked() throws Exception {
        mockMvc.perform(patch("/accounts/" + accountId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("status", "BLOCKED"))));

        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("100.00"), "dep-blocked")
                )))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Account is not active: " + accountId));
    }

    @Test
    void withdrawal_shouldReturn422WhenAccountIsClosed() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("500.00"), "dep-before-close")
                )));

        mockMvc.perform(patch("/accounts/" + accountId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("status", "CLOSED"))));

        mockMvc.perform(post("/transactions/withdrawal")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new WithdrawalRequest(accountId, new BigDecimal("100.00"), "wd-closed")
                )))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Account is not active: " + accountId));
    }
}
