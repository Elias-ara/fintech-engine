package com.fintechengine.modules.ledger.service;

import com.fintechengine.modules.ledger.dto.LedgerEntryResponse;
import com.fintechengine.modules.ledger.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> findByAccountId(UUID accountId) {
        return ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(LedgerEntryResponse::from)
                .toList();
    }
}
