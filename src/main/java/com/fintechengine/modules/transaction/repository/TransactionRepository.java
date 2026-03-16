package com.fintechengine.modules.transaction.repository;

import com.fintechengine.modules.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT DISTINCT t FROM Transaction t JOIN LedgerEntry le ON le.transaction = t WHERE le.account.id = :accountId ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountId(@Param("accountId") UUID accountId, Pageable pageable);
}
