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
import com.fintechengine.shared.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            LedgerEntryRepository ledgerEntryRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public TransactionResponse deposit(DepositRequest request) {
        var existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return TransactionResponse.from(existing.get());
        }

        Account account = requireActiveAccount(request.accountId().toString());

        Transaction tx = new Transaction(request.idempotencyKey(), Transaction.OperationType.DEPOSIT, request.amount());
        transactionRepository.save(tx);

        ledgerEntryRepository.save(new LedgerEntry(tx, account, LedgerEntry.Operation.CREDIT, request.amount()));

        account.setCachedBalance(account.getCachedBalance().add(request.amount()));
        accountRepository.save(account);

        tx.setStatus(Transaction.Status.COMPLETED);
        return TransactionResponse.from(transactionRepository.save(tx));
    }

    @Transactional
    public TransactionResponse withdrawal(WithdrawalRequest request) {
        var existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return TransactionResponse.from(existing.get());
        }

        Account account = requireActiveAccount(request.accountId().toString());

        if (account.getCachedBalance().compareTo(request.amount()) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        Transaction tx = new Transaction(request.idempotencyKey(), Transaction.OperationType.WITHDRAWAL, request.amount());
        transactionRepository.save(tx);

        ledgerEntryRepository.save(new LedgerEntry(tx, account, LedgerEntry.Operation.DEBIT, request.amount()));

        account.setCachedBalance(account.getCachedBalance().subtract(request.amount()));
        accountRepository.save(account);

        tx.setStatus(Transaction.Status.COMPLETED);
        return TransactionResponse.from(transactionRepository.save(tx));
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        var existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return TransactionResponse.from(existing.get());
        }

        if (request.sourceAccountId().equals(request.targetAccountId())) {
            throw new BusinessException("Source and target accounts must be different");
        }

        Account source = requireActiveAccount(request.sourceAccountId().toString());
        Account target = requireActiveAccount(request.targetAccountId().toString());

        if (source.getCachedBalance().compareTo(request.amount()) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        Transaction tx = new Transaction(request.idempotencyKey(), Transaction.OperationType.TRANSFER, request.amount());
        transactionRepository.save(tx);

        ledgerEntryRepository.save(new LedgerEntry(tx, source, LedgerEntry.Operation.DEBIT, request.amount()));
        ledgerEntryRepository.save(new LedgerEntry(tx, target, LedgerEntry.Operation.CREDIT, request.amount()));

        source.setCachedBalance(source.getCachedBalance().subtract(request.amount()));
        target.setCachedBalance(target.getCachedBalance().add(request.amount()));
        accountRepository.save(source);
        accountRepository.save(target);

        tx.setStatus(Transaction.Status.COMPLETED);
        return TransactionResponse.from(transactionRepository.save(tx));
    }

    private Account requireActiveAccount(String accountId) {
        Account account = accountRepository.findById(java.util.UUID.fromString(accountId))
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));
        if (account.getStatus() != Account.Status.ACTIVE) {
            throw new BusinessException("Account is not active: " + accountId);
        }
        return account;
    }
}
