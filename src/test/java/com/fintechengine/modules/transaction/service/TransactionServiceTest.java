package com.fintechengine.modules.transaction.service;

import com.fintechengine.modules.account.entity.Account;
import com.fintechengine.modules.account.repository.AccountRepository;
import com.fintechengine.modules.ledger.entity.LedgerEntry;
import com.fintechengine.modules.ledger.repository.LedgerEntryRepository;
import com.fintechengine.modules.transaction.dto.DepositRequest;
import com.fintechengine.modules.transaction.dto.TransactionResponse;
import com.fintechengine.modules.transaction.dto.TransferRequest;
import com.fintechengine.modules.transaction.dto.WithdrawalRequest;
import com.fintechengine.modules.transaction.entity.Transaction;
import com.fintechengine.modules.transaction.repository.TransactionRepository;
import com.fintechengine.modules.user.entity.User;
import com.fintechengine.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository accountRepository;
    @Mock LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks TransactionService transactionService;

    private Account account;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        User user = new User("João", "12345678901", "joao@email.com", "hashed");
        account = new Account(user);
        accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(ledgerEntryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void deposit_shouldCreditAccountAndCompleteTransaction() {
        when(transactionRepository.findByIdempotencyKey("dep-001")).thenReturn(Optional.empty());

        TransactionResponse response = transactionService.deposit(
                new DepositRequest(accountId, new BigDecimal("100.00"), "dep-001")
        );

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.operationType()).isEqualTo("DEPOSIT");
        assertThat(account.getCachedBalance()).isEqualByComparingTo("100.00");
        verify(ledgerEntryRepository).save(any(LedgerEntry.class));
    }

    @Test
    void withdrawal_shouldDebitAccountAndCompleteTransaction() {
        account.setCachedBalance(new BigDecimal("200.00"));
        when(transactionRepository.findByIdempotencyKey("saq-001")).thenReturn(Optional.empty());

        TransactionResponse response = transactionService.withdrawal(
                new WithdrawalRequest(accountId, new BigDecimal("50.00"), "saq-001")
        );

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(account.getCachedBalance()).isEqualByComparingTo("150.00");
        verify(ledgerEntryRepository).save(any(LedgerEntry.class));
    }

    @Test
    void withdrawal_shouldThrowWhenInsufficientBalance() {
        account.setCachedBalance(new BigDecimal("10.00"));
        when(transactionRepository.findByIdempotencyKey("saq-002")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.withdrawal(
                new WithdrawalRequest(accountId, new BigDecimal("50.00"), "saq-002")
        )).isInstanceOf(BusinessException.class)
          .hasMessage("Insufficient balance");
    }

    @Test
    void transfer_shouldDebitSourceAndCreditTarget() {
        User user2 = new User("Maria", "98765432100", "maria@email.com", "hashed");
        Account target = new Account(user2);
        UUID targetId = UUID.randomUUID();

        account.setCachedBalance(new BigDecimal("300.00"));
        when(accountRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(transactionRepository.findByIdempotencyKey("trf-001")).thenReturn(Optional.empty());

        TransactionResponse response = transactionService.transfer(
                new TransferRequest(accountId, targetId, new BigDecimal("100.00"), "trf-001")
        );

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(account.getCachedBalance()).isEqualByComparingTo("200.00");
        assertThat(target.getCachedBalance()).isEqualByComparingTo("100.00");
        verify(ledgerEntryRepository, times(2)).save(any(LedgerEntry.class));
    }

    @Test
    void deposit_shouldReturnExistingTransactionOnDuplicateIdempotencyKey() {
        Transaction existing = new Transaction("dep-001", Transaction.OperationType.DEPOSIT, new BigDecimal("100.00"));
        when(transactionRepository.findByIdempotencyKey("dep-001")).thenReturn(Optional.of(existing));

        transactionService.deposit(new DepositRequest(accountId, new BigDecimal("100.00"), "dep-001"));

        verify(transactionRepository, never()).save(any());
        verify(ledgerEntryRepository, never()).save(any());
    }
}
