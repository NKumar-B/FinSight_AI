package com.finsight.orchestrator.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.common.dto.ApiResponse;
import com.finsight.orchestrator.client.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);

    private final FinancialDataClient financialDataClient;
    private final ReconciliationClient reconciliationClient;
    private final FraudDetectionClient fraudDetectionClient;
    private final FinancialInsightsClient financialInsightsClient;
    private final AuditClient auditClient;
    private final ObjectMapper objectMapper;

    private final Map<String, ToolDefinition> tools = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        registerTool("fetch_account_summary", "Retrieves active accounts and current ledger balances", "FINANCIAL_DATA");
        registerTool("fetch_unreconciled_transactions", "Fetches all pending unreconciled transactions across accounts", "FINANCIAL_DATA");
        registerTool("trigger_reconciliation_match", "Triggers deterministic matching run across internal & counterpart records", "RECONCILIATION");
        registerTool("evaluate_transaction_fraud", "Runs real-time fraud rules engine on a transaction to determine risk score", "FRAUD_DETECTION");
        registerTool("get_fraud_alerts", "Queries active fraud alerts and risk severity statistics", "FRAUD_DETECTION");
        registerTool("get_ar_aging_summary", "Retrieves Accounts Receivable aging buckets and overdue customer invoices", "INSIGHTS");
        registerTool("get_expense_breakdown", "Aggregates historical debit spend by category and top merchants", "INSIGHTS");
        registerTool("get_cash_flow_forecast", "Models cash balance runway and projected inflows/outflows over N days", "INSIGHTS");
        registerTool("query_audit_trail", "Searches immutable system audit log for events, actors, and state transitions", "AUDIT");
    }

    private void registerTool(String name, String description, String category) {
        tools.put(name, ToolDefinition.builder()
                .name(name)
                .description(description)
                .category(category)
                .build());
    }

    public List<ToolDefinition> listTools() {
        return new ArrayList<>(tools.values());
    }

    public ToolExecutionResult executeTool(String toolName, Map<String, Object> params) {
        log.info("[ToolRegistry] Executing tool '{}' with params: {}", toolName, params);
        if (!tools.containsKey(toolName)) {
            return ToolExecutionResult.builder()
                    .toolName(toolName)
                    .success(false)
                    .message("Tool '" + toolName + "' is not registered in tool registry.")
                    .build();
        }

        try {
            switch (toolName) {
                case "fetch_account_summary" -> {
                    var resp = financialDataClient.getAccounts(50);
                    return ToolExecutionResult.builder().toolName(toolName).success(true).data(resp.getData()).build();
                }
                case "fetch_unreconciled_transactions" -> {
                    var resp = financialDataClient.getUnreconciledTransactions();
                    return ToolExecutionResult.builder().toolName(toolName).success(true).data(resp.getData()).build();
                }
                case "trigger_reconciliation_match" -> {
                    var resp = reconciliationClient.executeReconciliation(params != null ? params : Collections.emptyMap());
                    return ToolExecutionResult.builder().toolName(toolName).success(true).data(resp.getData()).build();
                }
                case "evaluate_transaction_fraud" -> {
                    var resp = fraudDetectionClient.evaluateTransaction(params != null ? params : Collections.emptyMap());
                    return ToolExecutionResult.builder().toolName(toolName).success(true).data(resp.getData()).build();
                }
                case "get_fraud_alerts" -> {
                    var resp = fraudDetectionClient.listAlerts(20);
                    return ToolExecutionResult.builder().toolName(toolName).success(true).data(resp.getData()).build();
                }
                case "get_ar_aging_summary" -> {
                    var resp = financialInsightsClient.getArAging();
                    return ToolExecutionResult.builder().toolName(toolName).success(true).data(resp.getData()).build();
                }
                case "get_expense_breakdown" -> {
                    var resp = financialInsightsClient.getExpenseAnalysis();
                    return ToolExecutionResult.builder().toolName(toolName).success(true).data(resp.getData()).build();
                }
                case "get_cash_flow_forecast" -> {
                    var resp = financialInsightsClient.getCashFlowForecast(params != null ? params : Collections.emptyMap());
                    return ToolExecutionResult.builder().toolName(toolName).success(true).data(resp.getData()).build();
                }
                case "query_audit_trail" -> {
                    var resp = auditClient.getEvents(20);
                    return ToolExecutionResult.builder().toolName(toolName).success(true).data(resp.getData()).build();
                }
                default -> {
                    return ToolExecutionResult.builder().toolName(toolName).success(false).message("Unhandled tool handler").build();
                }
            }
        } catch (Exception e) {
            log.error("[ToolRegistry] Error executing tool '{}': {}", toolName, e.getMessage(), e);
            return ToolExecutionResult.builder()
                    .toolName(toolName)
                    .success(false)
                    .message("Failed executing tool: " + e.getMessage())
                    .build();
        }
    }
}
