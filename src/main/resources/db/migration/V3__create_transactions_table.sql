CREATE TABLE transactions (
    id              UUID           PRIMARY KEY,
    idempotency_key VARCHAR(255)   NOT NULL UNIQUE,
    operation_type  VARCHAR(20)    NOT NULL,
    amount          NUMERIC(19, 4) NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP      NOT NULL DEFAULT now(),

    CONSTRAINT chk_transaction_amount          CHECK (amount > 0),
    CONSTRAINT chk_transaction_operation_type  CHECK (operation_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    CONSTRAINT chk_transaction_status          CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_transactions_idempotency_key ON transactions (idempotency_key);
