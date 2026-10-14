-- FinSight AI: AI Orchestrator Service Schema V1
CREATE TABLE IF NOT EXISTS agent_runs (
    id BIGSERIAL PRIMARY KEY,
    run_reference VARCHAR(64) NOT NULL UNIQUE,
    agent_type VARCHAR(50) NOT NULL,
    user_prompt TEXT NOT NULL,
    plan TEXT,
    tool_calls_history TEXT,
    final_synthesis TEXT,
    confidence_score DOUBLE PRECISION NOT NULL DEFAULT 0.95,
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    requires_human_approval BOOLEAN NOT NULL DEFAULT FALSE,
    approval_reason VARCHAR(500),
    action_to_approve TEXT,
    reviewed_by VARCHAR(100),
    review_notes VARCHAR(1000),
    initiated_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_agent_run_ref ON agent_runs (run_reference);
CREATE INDEX IF NOT EXISTS idx_agent_type ON agent_runs (agent_type);
CREATE INDEX IF NOT EXISTS idx_agent_status ON agent_runs (status);
CREATE INDEX IF NOT EXISTS idx_agent_created_at ON agent_runs (created_at);
