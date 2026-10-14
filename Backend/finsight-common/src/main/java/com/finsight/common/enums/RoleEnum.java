package com.finsight.common.enums;

public enum RoleEnum {
    ROLE_ADMIN("Full system administration"),
    ROLE_COMPLIANCE_OFFICER("Regulatory, fraud review, and compliance operations"),
    ROLE_FINANCIAL_ANALYST("Financial ingestion, insights, and analysis"),
    ROLE_AUDITOR("Read-only audit trail and report inspection"),
    ROLE_REVIEWER("Reconciliation discrepancy review and approvals"),
    ROLE_USER("Standard business user");

    private final String description;

    RoleEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
