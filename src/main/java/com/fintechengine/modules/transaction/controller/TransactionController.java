package com.fintechengine.modules.transaction.controller;

import com.fintechengine.modules.transaction.dto.DepositRequest;
import com.fintechengine.modules.transaction.dto.TransactionResponse;
import com.fintechengine.modules.transaction.dto.TransferRequest;
import com.fintechengine.modules.transaction.dto.WithdrawalRequest;
import com.fintechengine.modules.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Transactions", description = "Deposits, withdrawals and transfers")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Deposit funds into an account")
    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse deposit(@Valid @RequestBody DepositRequest request) {
        return transactionService.deposit(request);
    }

    @Operation(summary = "Withdraw funds from an account")
    @PostMapping("/withdrawal")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse withdrawal(@Valid @RequestBody WithdrawalRequest request) {
        return transactionService.withdrawal(request);
    }

    @Operation(summary = "Transfer funds between two accounts")
    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(request);
    }
}
