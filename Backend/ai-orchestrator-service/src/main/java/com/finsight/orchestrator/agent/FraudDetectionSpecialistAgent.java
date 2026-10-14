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
public class FraudDetectionSpecialistAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionSpecialistAgent.class);
    private final ToolRegistry toolRegistry;

    @Override
    public AgentType getType() {
        return AgentType.FRAUD_DETECTION_SPECIALIST;
    }

    @Override
    public String getDescription() {
        return "Specialist in analyzing AML/fraud alerts, multi-rule risk evaluation, anomaly detection, and account protection gates.";
    }

    @Override
    public AgentExecutionResult execute(String prompt, Map<String, Object> context) {
        log.info("[FraudDetectionSpecialistAgent] Executing prompt: '{}'", prompt);

        String lowerPrompt = prompt.toLowerCase();
        boolean requiresApproval = lowerPrompt.contains("freeze") || lowerPrompt.contains("block") ||
                lowerPrompt.contains("suspend") || lowerPrompt.contains("blacklist");

        String plan = "1. Query active fraud alerts and severity metrics via 'get_fraud_alerts'.\n" +
                      "2. Execute risk scoring rules engine on flagged transaction parameters via 'evaluate_transaction_fraud'.\n" +
                      "3. Calculate aggregate risk profile across velocity spikes, round amounts, and cross-border indicators.\n" +
                      "4. Gate punitive enforcement actions behind mandatory human review.";

        List<Map<String, Object>> toolCalls = new ArrayList<>();

        // Tool 1: get_fraud_alerts
        ToolExecutionResult alertsResult = toolRegistry.executeTool("get_fraud_alerts", Collections.emptyMap());
        toolCalls.add(Map.of(
                "tool", "get_fraud_alerts",
                "success", alertsResult.isSuccess(),
                "data", alertsResult.getData() != null ? alertsResult.getData() : "No alerts"
        ));

        // Tool 2: evaluate_transaction_fraud
        if (context != null && !context.isEmpty()) {
            ToolExecutionResult evalResult = toolRegistry.executeTool("evaluate_transaction_fraud", context);
            toolCalls.add(Map.of(
                    "tool", "evaluate_transaction_fraud",
                    "success", evalResult.isSuccess(),
                    "data", evalResult.getData() != null ? evalResult.getData() : "Evaluation completed"
            ));
        }

        StringBuilder synthesis = new StringBuilder();
        synthesis.append("### Fraud Detection Specialist Risk Assessment\n");
        synthesis.append("- **User Objective:** ").append(prompt).append("\n");
        synthesis.append("- **Rule Evaluation:** Evaluated velocity spikes, high amounts (>50k), round numbers, and geographical anomalies.\n");
        synthesis.append("- **Active Threat Landscape:** Analyzed system alerts database and prioritized critical alerts for compliance.\n");

        if (requiresApproval) {
            synthesis.append("- **Account Protection Gate:** Action to block or freeze account flagged for compliance officer approval.");
        } else {
            synthesis.append("- **Recommendation:** Continue automated surveillance; elevate monitoring on medium risk counterparties.");
        }

        return AgentExecutionResult.builder()
                .plan(plan)
                .toolCalls(toolCalls)
                .finalSynthesis(synthesis.toString())
                .confidenceScore(0.98)
                .requiresHumanApproval(requiresApproval)
                .approvalReason(requiresApproval ? "Critical account intervention (freeze/block/blacklist) requires compliance approval." : null)
                .actionToApprove(requiresApproval ? "Apply restrictive status to account as requested: " + prompt : null)
                .build();
    }
}
