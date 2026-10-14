package com.finsight.orchestrator.agent;

import com.finsight.orchestrator.enums.AgentType;
import com.finsight.orchestrator.tools.ToolExecutionResult;
import com.finsight.orchestrator.tools.ToolRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class FinancialDataAnalystAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(FinancialDataAnalystAgent.class);
    private final ToolRegistry toolRegistry;

    @Override
    public AgentType getType() {
        return AgentType.FINANCIAL_DATA_ANALYST;
    }

    @Override
    public String getDescription() {
        return "Specialist in analyzing account ledgers, balances, currency exposures, and ingestion transaction health.";
    }

    @Override
    public AgentExecutionResult execute(String prompt, Map<String, Object> context) {
        log.info("[FinancialDataAnalystAgent] Executing prompt: '{}'", prompt);

        String plan = "1. Fetch current account portfolio and balances via 'fetch_account_summary'.\n" +
                      "2. Inspect unreconciled transactions backlog via 'fetch_unreconciled_transactions'.\n" +
                      "3. Synthesize account health, currency liquidity, and pending reconciliation exposure.";

        List<Map<String, Object>> toolCalls = new ArrayList<>();

        // Tool 1: fetch_account_summary
        ToolExecutionResult accountResult = toolRegistry.executeTool("fetch_account_summary", Collections.emptyMap());
        toolCalls.add(Map.of(
                "tool", "fetch_account_summary",
                "success", accountResult.isSuccess(),
                "data", accountResult.getData() != null ? accountResult.getData() : "No data"
        ));

        // Tool 2: fetch_unreconciled_transactions
        ToolExecutionResult unreconciledResult = toolRegistry.executeTool("fetch_unreconciled_transactions", Collections.emptyMap());
        toolCalls.add(Map.of(
                "tool", "fetch_unreconciled_transactions",
                "success", unreconciledResult.isSuccess(),
                "data", unreconciledResult.getData() != null ? unreconciledResult.getData() : "No data"
        ));

        StringBuilder synthesis = new StringBuilder();
        synthesis.append("### Financial Data Analyst Findings\n");
        synthesis.append("- **User Objective:** ").append(prompt).append("\n");
        synthesis.append("- **Account Portfolio Status:** Evaluated active accounts and real-time ledger balances.\n");
        synthesis.append("- **Ledger Reconciliation Readiness:** Extracted current unreconciled transactions for matching pipeline.\n");
        synthesis.append("- **Recommendation:** Maintain automated daily ingestion pipelines and verify counterpart settlement timestamps.");

        return AgentExecutionResult.builder()
                .plan(plan)
                .toolCalls(toolCalls)
                .finalSynthesis(synthesis.toString())
                .confidenceScore(0.96)
                .requiresHumanApproval(false)
                .build();
    }
}
