CREATE TABLE users (
    id         UUID         PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    document   VARCHAR(14)  NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_document ON users (document);
CREATE INDEX idx_users_email ON users (email);
