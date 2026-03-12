CREATE TABLE accounts (
    id             UUID           PRIMARY KEY,
    user_id        UUID           NOT NULL REFERENCES users (id),
    status         VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    cached_balance NUMERIC(19, 4) NOT NULL DEFAULT 0,
    created_at     TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP      NOT NULL DEFAULT now(),

    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED'))
);

CREATE INDEX idx_accounts_user_id ON accounts (user_id);
