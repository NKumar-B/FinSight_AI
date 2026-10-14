-- FinSight AI: Financial Data Service Initial Schema
CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    account_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(30) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    institution_name VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS import_batches (
    id BIGSERIAL PRIMARY KEY,
    batch_reference VARCHAR(64) NOT NULL UNIQUE,
    file_name VARCHAR(255) NOT NULL,
    file_checksum_sha256 VARCHAR(64) NOT NULL,
    account_id BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_records INT NOT NULL DEFAULT 0,
    successful_records INT NOT NULL DEFAULT 0,
    failed_records INT NOT NULL DEFAULT 0,
    error_details TEXT,
    imported_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
    transaction_reference VARCHAR(100) NOT NULL,
    transaction_date DATE NOT NULL,
    value_date DATE,
    amount NUMERIC(18, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(100),
    merchant_name VARCHAR(150),
    counterpart_account VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'POSTED',
    import_batch_id BIGINT REFERENCES import_batches(id) ON DELETE SET NULL,
    fingerprint VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_account_number ON accounts(account_number);
CREATE INDEX IF NOT EXISTS idx_account_status ON accounts(status);
CREATE INDEX IF NOT EXISTS idx_batch_reference ON import_batches(batch_reference);
CREATE INDEX IF NOT EXISTS idx_batch_checksum ON import_batches(file_checksum_sha256);
CREATE INDEX IF NOT EXISTS idx_txn_account_id ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_txn_reference ON transactions(transaction_reference);
CREATE INDEX IF NOT EXISTS idx_txn_date ON transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_txn_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_txn_fingerprint ON transactions(fingerprint);
