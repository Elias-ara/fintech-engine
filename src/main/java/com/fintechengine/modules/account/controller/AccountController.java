package com.fintechengine.modules.account.controller;

import com.fintechengine.modules.account.dto.AccountResponse;
import com.fintechengine.modules.account.dto.CreateAccountRequest;
import com.fintechengine.modules.account.dto.UpdateAccountStatusRequest;
import com.fintechengine.modules.account.service.AccountService;
import com.fintechengine.modules.ledger.dto.LedgerEntryResponse;
import com.fintechengine.modules.ledger.service.LedgerService;
import com.fintechengine.modules.transaction.dto.TransactionResponse;
import com.fintechengine.modules.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Accounts", description = "Account management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final LedgerService ledgerService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, LedgerService ledgerService,
                             TransactionService transactionService) {
        this.accountService = accountService;
        this.ledgerService = ledgerService;
        this.transactionService = transactionService;
    }

    @Operation(summary = "Create a new account for a user")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.create(request);
    }

    @Operation(summary = "Get account details and current balance")
    @GetMapping("/{id}")
    public AccountResponse findById(@PathVariable UUID id) {
        return accountService.findById(id);
    }

    @Operation(summary = "Update account status (ACTIVE, BLOCKED, CLOSED)")
    @PatchMapping("/{id}/status")
    public AccountResponse updateStatus(@PathVariable UUID id,
                                        @Valid @RequestBody UpdateAccountStatusRequest request) {
        return accountService.updateStatus(id, request);
    }

    @Operation(summary = "Get transactions for an account")
    @GetMapping("/{id}/transactions")
    public Page<TransactionResponse> getTransactions(@PathVariable UUID id,
                                                     @PageableDefault(size = 20) Pageable pageable) {
        return transactionService.findByAccountId(id, pageable);
    }

    @Operation(summary = "Get ledger entries (statement) for an account")
    @GetMapping("/{id}/ledger")
    public Page<LedgerEntryResponse> getLedger(@PathVariable UUID id,
                                               @PageableDefault(size = 20) Pageable pageable) {
        return ledgerService.findByAccountId(id, pageable);
    }
}
