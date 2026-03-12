package com.fintechengine.modules.account.service;

import com.fintechengine.modules.account.dto.AccountResponse;
import com.fintechengine.modules.account.dto.CreateAccountRequest;
import com.fintechengine.modules.account.entity.Account;
import com.fintechengine.modules.account.repository.AccountRepository;
import com.fintechengine.modules.user.entity.User;
import com.fintechengine.modules.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.userId()));
        Account account = new Account(user);
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(UUID id) {
        return accountRepository.findById(id)
                .map(AccountResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + id));
    }
}
