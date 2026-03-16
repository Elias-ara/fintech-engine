package com.fintechengine.modules.ledger.service;

import com.fintechengine.modules.ledger.dto.LedgerEntryResponse;
import com.fintechengine.modules.ledger.repository.LedgerEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> findByAccountId(UUID accountId, Pageable pageable) {
        return ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(LedgerEntryResponse::from);
    }
}
