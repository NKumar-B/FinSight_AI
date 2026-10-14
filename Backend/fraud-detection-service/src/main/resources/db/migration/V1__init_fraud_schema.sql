-- FinSight AI - Fraud Detection Service Schema Initial Migration

CREATE TABLE IF NOT EXISTS fraud_alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_reference VARCHAR(64) NOT NULL UNIQUE,
    transaction_id BIGINT NOT NULL,
    transaction_reference VARCHAR(100),
    account_id BIGINT NOT NULL,
    account_number VARCHAR(50),
    amount NUMERIC(18, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    risk_score INT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    triggered_rules VARCHAR(500) NOT NULL,
    details VARCHAR(2000),
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    assigned_to VARCHAR(100),
    resolution_notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    resolved_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_alert_reference ON fraud_alerts(alert_reference);
CREATE INDEX IF NOT EXISTS idx_alert_tx_id ON fraud_alerts(transaction_id);
CREATE INDEX IF NOT EXISTS idx_alert_account_id ON fraud_alerts(account_id);
CREATE INDEX IF NOT EXISTS idx_alert_status ON fraud_alerts(status);
CREATE INDEX IF NOT EXISTS idx_alert_severity ON fraud_alerts(severity);
