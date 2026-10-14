-- FinSight AI - Reconciliation Service Schema Initial Migration

CREATE TABLE IF NOT EXISTS reconciliation_runs (
    id BIGSERIAL PRIMARY KEY,
    run_reference VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    total_source_records INT NOT NULL DEFAULT 0,
    total_target_records INT NOT NULL DEFAULT 0,
    exact_matches_count INT NOT NULL DEFAULT 0,
    tolerance_matches_count INT NOT NULL DEFAULT 0,
    discrepancies_count INT NOT NULL DEFAULT 0,
    unmatched_count INT NOT NULL DEFAULT 0,
    date_tolerance_days INT NOT NULL DEFAULT 2,
    amount_tolerance NUMERIC(18, 2) NOT NULL DEFAULT 0.05,
    executed_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_recon_reference ON reconciliation_runs(run_reference);
CREATE INDEX IF NOT EXISTS idx_recon_status ON reconciliation_runs(status);

CREATE TABLE IF NOT EXISTS reconciliation_matches (
    id BIGSERIAL PRIMARY KEY,
    run_id BIGINT NOT NULL REFERENCES reconciliation_runs(id) ON DELETE CASCADE,
    source_transaction_id BIGINT NOT NULL,
    source_transaction_ref VARCHAR(100) NOT NULL,
    target_transaction_id BIGINT,
    target_transaction_ref VARCHAR(100),
    target_transaction_ids VARCHAR(500),
    match_type VARCHAR(30) NOT NULL,
    amount_difference NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
    date_difference_days INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'AUTOMATICALLY_MATCHED',
    reviewed_by VARCHAR(100),
    review_notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_match_run_id ON reconciliation_matches(run_id);
CREATE INDEX IF NOT EXISTS idx_match_source_id ON reconciliation_matches(source_transaction_id);
CREATE INDEX IF NOT EXISTS idx_match_target_id ON reconciliation_matches(target_transaction_id);
CREATE INDEX IF NOT EXISTS idx_match_type ON reconciliation_matches(match_type);
CREATE INDEX IF NOT EXISTS idx_match_status ON reconciliation_matches(status);

CREATE TABLE IF NOT EXISTS reconciliation_discrepancies (
    id BIGSERIAL PRIMARY KEY,
    run_id BIGINT NOT NULL REFERENCES reconciliation_runs(id) ON DELETE CASCADE,
    case_reference VARCHAR(64) NOT NULL UNIQUE,
    discrepancy_type VARCHAR(40) NOT NULL,
    source_transaction_id BIGINT,
    source_transaction_ref VARCHAR(100),
    target_transaction_id BIGINT,
    target_transaction_ref VARCHAR(100),
    source_amount NUMERIC(18, 2),
    target_amount NUMERIC(18, 2),
    amount_discrepancy NUMERIC(18, 2),
    source_date DATE,
    target_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    assigned_to VARCHAR(100),
    resolution_notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_case_reference ON reconciliation_discrepancies(case_reference);
CREATE INDEX IF NOT EXISTS idx_case_run_id ON reconciliation_discrepancies(run_id);
CREATE INDEX IF NOT EXISTS idx_case_status ON reconciliation_discrepancies(status);
CREATE INDEX IF NOT EXISTS idx_case_type ON reconciliation_discrepancies(discrepancy_type);

CREATE TABLE IF NOT EXISTS review_decisions (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT,
    match_id BIGINT,
    decision VARCHAR(40) NOT NULL,
    reviewer_id VARCHAR(100) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    before_state TEXT,
    after_state TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_decision_case_id ON review_decisions(case_id);
CREATE INDEX IF NOT EXISTS idx_decision_match_id ON review_decisions(match_id);
