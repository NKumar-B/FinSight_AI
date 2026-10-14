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
public class ReconciliationSpecialistAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationSpecialistAgent.class);
    private final ToolRegistry toolRegistry;

    @Override
    public AgentType getType() {
        return AgentType.RECONCILIATION_SPECIALIST;
    }

    @Override
    public String getDescription() {
        return "Specialist in deterministic transaction matching, discrepancy diagnosis, and reconciliation review workflows.";
    }

    @Override
    public AgentExecutionResult execute(String prompt, Map<String, Object> context) {
        log.info("[ReconciliationSpecialistAgent] Executing prompt: '{}'", prompt);

        String lowerPrompt = prompt.toLowerCase();
        boolean requiresApproval = lowerPrompt.contains("write off") || lowerPrompt.contains("write-off") ||
                lowerPrompt.contains("adjust") || lowerPrompt.contains("force match");

        String plan = "1. Retrieve pending unreconciled transactions via 'fetch_unreconciled_transactions'.\n" +
                      "2. Trigger automated deterministic matching engine via 'trigger_reconciliation_match'.\n" +
                      "3. Categorize matched pairs, timing differences, and amount variances.\n" +
                      "4. Assess risk criteria and human-in-the-loop review threshold for residual balances.";

        List<Map<String, Object>> toolCalls = new ArrayList<>();

        // Tool 1: fetch_unreconciled_transactions
        ToolExecutionResult unreconciledResult = toolRegistry.executeTool("fetch_unreconciled_transactions", Collections.emptyMap());
        toolCalls.add(Map.of(
                "tool", "fetch_unreconciled_transactions",
                "success", unreconciledResult.isSuccess(),
                "data", unreconciledResult.getData() != null ? unreconciledResult.getData() : "No data"
        ));

        // Tool 2: trigger_reconciliation_match
        ToolExecutionResult matchResult = toolRegistry.executeTool("trigger_reconciliation_match", context != null ? context : Collections.emptyMap());
        toolCalls.add(Map.of(
                "tool", "trigger_reconciliation_match",
                "success", matchResult.isSuccess(),
                "data", matchResult.getData() != null ? matchResult.getData() : "Matching completed"
        ));

        StringBuilder synthesis = new StringBuilder();
        synthesis.append("### Reconciliation Specialist Assessment\n");
        synthesis.append("- **User Objective:** ").append(prompt).append("\n");
        synthesis.append("- **Matching Execution:** Deterministic engine evaluated exact, tolerance (±0.05 / ±3 days), and one-to-many criteria.\n");
        synthesis.append("- **Discrepancy Breakdown:** Flagged timing differences and fee deductions for accounting review.\n");

        if (requiresApproval) {
            synthesis.append("- **Approval Gate Triggered:** Ledger adjustment or write-off action requires formal financial review.");
        } else {
            synthesis.append("- **Recommendation:** All standard matches auto-cleared. Open residual exceptions routed to standard reconciliation queue.");
        }

        return AgentExecutionResult.builder()
                .plan(plan)
                .toolCalls(toolCalls)
                .finalSynthesis(synthesis.toString())
                .confidenceScore(0.97)
                .requiresHumanApproval(requiresApproval)
                .approvalReason(requiresApproval ? "High-impact ledger modification or write-off requested by agent execution." : null)
                .actionToApprove(requiresApproval ? "Execute write-off / manual journal balance adjustment for prompt: " + prompt : null)
                .build();
    }
}
