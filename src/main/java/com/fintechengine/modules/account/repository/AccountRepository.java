package com.fintechengine.modules.account.repository;

import com.fintechengine.modules.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}
