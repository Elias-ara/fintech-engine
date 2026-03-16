package com.fintechengine.modules.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintechengine.modules.account.entity.Account;
import com.fintechengine.modules.account.repository.AccountRepository;
import com.fintechengine.modules.auth.dto.LoginRequest;
import com.fintechengine.modules.transaction.dto.DepositRequest;
import com.fintechengine.modules.transaction.dto.TransferRequest;
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
        User user = new User("João", "12345678901", "joao@email.com", passwordEncoder.encode("senha123"));
        userRepository.save(user);

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

    // ========== POST /transactions/deposit ==========

    @Test
    void deposit_shouldReturn201AndIncreaseBalance() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("200.00"), "dep-001")
                )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.operationType").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(200.00));

        mockMvc.perform(get("/accounts/" + accountId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(200.00));
    }

    @Test
    void deposit_shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("100.00"), "dep-unauth")
                )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deposit_shouldReturn422WhenAccountNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(randomId, new BigDecimal("100.00"), "dep-notfound")
                )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account not found: " + randomId));
    }

    @Test
    void deposit_shouldBeIdempotent() throws Exception {
        var request = new DepositRequest(accountId, new BigDecimal("100.00"), "dep-idem");

        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/accounts/" + accountId)
                .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void deposit_shouldReturn400WhenAmountIsNegative() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("-100.00"), "dep-negative")
                )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deposit_shouldReturn400WhenIdempotencyKeyIsBlank() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("100.00"), "")
                )))
                .andExpect(status().isBadRequest());
    }

    // ========== POST /transactions/withdrawal ==========

    @Test
    void withdrawal_shouldReturn201AndDecreaseBalance() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("500.00"), "dep-before-withdraw")
                )))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/transactions/withdrawal")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new WithdrawalRequest(accountId, new BigDecimal("200.00"), "withdraw-001")
                )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.operationType").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.amount").value(200.00));

        mockMvc.perform(get("/accounts/" + accountId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(300.00));
    }

    @Test
    void withdrawal_shouldReturn422WhenInsufficientBalance() throws Exception {
        mockMvc.perform(post("/transactions/withdrawal")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new WithdrawalRequest(accountId, new BigDecimal("500.00"), "withdraw-insuf")
                )))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Insufficient balance"));
    }

    @Test
    void withdrawal_shouldBeIdempotent() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("200.00"), "dep-before-idem-withdraw")
                )))
                .andExpect(status().isCreated());

        var withdrawRequest = new WithdrawalRequest(accountId, new BigDecimal("100.00"), "withdraw-idem");

        mockMvc.perform(post("/transactions/withdrawal")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/transactions/withdrawal")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/accounts/" + accountId)
                .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void withdrawal_shouldReturn422WhenAccountNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(post("/transactions/withdrawal")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new WithdrawalRequest(randomId, new BigDecimal("100.00"), "withdraw-notfound")
                )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account not found: " + randomId));
    }

    // ========== POST /transactions/transfer ==========

    @Test
    void transfer_shouldTransferBetweenAccounts() throws Exception {
        User user2 = new User("Maria", "98765432100", "maria@email.com", passwordEncoder.encode("senha123"));
        userRepository.save(user2);
        Account account2 = new Account(user2);
        accountRepository.save(account2);
        UUID account2Id = account2.getId();

        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("500.00"), "dep-before-transfer")
                )))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new TransferRequest(accountId, account2Id, new BigDecimal("200.00"), "transfer-001")
                )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.operationType").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(200.00));

        mockMvc.perform(get("/accounts/" + accountId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(300.00));

        mockMvc.perform(get("/accounts/" + account2Id)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(200.00));
    }

    @Test
    void transfer_shouldReturn422WhenInsufficientBalance() throws Exception {
        User user2 = new User("Maria", "98765432100", "maria@email.com", passwordEncoder.encode("senha123"));
        userRepository.save(user2);
        Account account2 = new Account(user2);
        accountRepository.save(account2);
        UUID account2Id = account2.getId();

        mockMvc.perform(post("/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new TransferRequest(accountId, account2Id, new BigDecimal("500.00"), "transfer-insuf")
                )))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Insufficient balance"));
    }

    @Test
    void transfer_shouldReturn422WhenSourceEqualsTarget() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new TransferRequest(accountId, accountId, new BigDecimal("100.00"), "transfer-same")
                )))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Source and target accounts must be different"));
    }

    @Test
    void transfer_shouldBeIdempotent() throws Exception {
        User user2 = new User("Maria", "98765432100", "maria@email.com", passwordEncoder.encode("senha123"));
        userRepository.save(user2);
        Account account2 = new Account(user2);
        accountRepository.save(account2);
        UUID account2Id = account2.getId();

        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("500.00"), "dep-before-idem-transfer")
                )))
                .andExpect(status().isCreated());

        var transferRequest = new TransferRequest(accountId, account2Id, new BigDecimal("200.00"), "transfer-idem");

        mockMvc.perform(post("/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/accounts/" + accountId)
                .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.balance").value(300.00));

        mockMvc.perform(get("/accounts/" + account2Id)
                .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.balance").value(200.00));
    }

    // ========== GET /transactions/{id} ==========

    @Test
    void findById_shouldReturnTransaction() throws Exception {
        MvcResult depositResult = mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("100.00"), "dep-find")
                ))).andReturn();

        String txId = objectMapper.readTree(depositResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(get("/transactions/" + txId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(txId))
                .andExpect(jsonPath("$.operationType").value("DEPOSIT"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/transactions/" + randomId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Transaction not found: " + randomId));
    }

    // ========== GET /accounts/{id}/transactions ==========

    @Test
    void getTransactions_shouldReturnPagedResults() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("100.00"), "dep-page-1")
                )))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(
                        new DepositRequest(accountId, new BigDecimal("50.00"), "dep-page-2")
                )))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/accounts/" + accountId + "/transactions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}
