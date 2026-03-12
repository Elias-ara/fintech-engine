CREATE TABLE ledger_entries (
    id             UUID           PRIMARY KEY,
    transaction_id UUID           NOT NULL REFERENCES transactions (id),
    account_id     UUID           NOT NULL REFERENCES accounts (id),
    operation      VARCHAR(10)    NOT NULL,
    amount         NUMERIC(19, 4) NOT NULL,
    created_at     TIMESTAMP      NOT NULL DEFAULT now(),

    CONSTRAINT chk_ledger_amount    CHECK (amount > 0),
    CONSTRAINT chk_ledger_operation CHECK (operation IN ('CREDIT', 'DEBIT'))
);

CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries (transaction_id);
CREATE INDEX idx_ledger_entries_account_id     ON ledger_entries (account_id);
