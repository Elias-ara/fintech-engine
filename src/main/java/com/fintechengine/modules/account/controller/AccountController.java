package com.fintechengine.modules.account.controller;

import com.fintechengine.modules.account.dto.AccountResponse;
import com.fintechengine.modules.account.dto.CreateAccountRequest;
import com.fintechengine.modules.account.service.AccountService;
import com.fintechengine.modules.ledger.dto.LedgerEntryResponse;
import com.fintechengine.modules.ledger.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final LedgerService ledgerService;

    public AccountController(AccountService accountService, LedgerService ledgerService) {
        this.accountService = accountService;
        this.ledgerService = ledgerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.create(request);
    }

    @GetMapping("/{id}")
    public AccountResponse findById(@PathVariable UUID id) {
        return accountService.findById(id);
    }

    @GetMapping("/{id}/ledger")
    public List<LedgerEntryResponse> getLedger(@PathVariable UUID id) {
        return ledgerService.findByAccountId(id);
    }
}
