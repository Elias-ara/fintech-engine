package com.fintechengine.modules.account.controller;

import com.fintechengine.modules.account.dto.AccountResponse;
import com.fintechengine.modules.account.dto.CreateAccountRequest;
import com.fintechengine.modules.account.service.AccountService;
import com.fintechengine.modules.ledger.dto.LedgerEntryResponse;
import com.fintechengine.modules.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Accounts", description = "Account management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final LedgerService ledgerService;

    public AccountController(AccountService accountService, LedgerService ledgerService) {
        this.accountService = accountService;
        this.ledgerService = ledgerService;
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

    @Operation(summary = "Get ledger entries (statement) for an account")
    @GetMapping("/{id}/ledger")
    public List<LedgerEntryResponse> getLedger(@PathVariable UUID id) {
        return ledgerService.findByAccountId(id);
    }
}
