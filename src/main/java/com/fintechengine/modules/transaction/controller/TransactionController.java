package com.fintechengine.modules.transaction.controller;

import com.fintechengine.modules.transaction.dto.DepositRequest;
import com.fintechengine.modules.transaction.dto.TransactionResponse;
import com.fintechengine.modules.transaction.dto.TransferRequest;
import com.fintechengine.modules.transaction.dto.WithdrawalRequest;
import com.fintechengine.modules.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse deposit(@Valid @RequestBody DepositRequest request) {
        return transactionService.deposit(request);
    }

    @PostMapping("/withdrawal")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse withdrawal(@Valid @RequestBody WithdrawalRequest request) {
        return transactionService.withdrawal(request);
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(request);
    }
}
