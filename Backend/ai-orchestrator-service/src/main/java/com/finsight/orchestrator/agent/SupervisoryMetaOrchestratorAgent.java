package com.finsight.orchestrator.agent;

import com.finsight.orchestrator.enums.AgentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SupervisoryMetaOrchestratorAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(SupervisoryMetaOrchestratorAgent.class);

    private final Map<AgentType, Agent> agentRegistry = new EnumMap<>(AgentType.class);

    public SupervisoryMetaOrchestratorAgent(@Lazy List<Agent> agents) {
        for (Agent a : agents) {
            if (a.getType() != AgentType.SUPERVISORY_META_ORCHESTRATOR) {
                agentRegistry.put(a.getType(), a);
            }
        }
    }

    @Override
    public AgentType getType() {
        return AgentType.SUPERVISORY_META_ORCHESTRATOR;
    }

    @Override
    public String getDescription() {
        return "Master supervisory agent that classifies intent, delegates tasks to domain specialists, and enforces human-in-the-loop review governance.";
    }

    @Override
    public AgentExecutionResult execute(String prompt, Map<String, Object> context) {
        log.info("[SupervisoryMetaOrchestratorAgent] Classifying intent for prompt: '{}'", prompt);

        AgentType selectedSpecialist = routeIntent(prompt);
        log.info("[SupervisoryMetaOrchestratorAgent] Routing request to specialist: {}", selectedSpecialist);

        Agent specialist = agentRegistry.get(selectedSpecialist);
        if (specialist != null) {
            AgentExecutionResult subResult = specialist.execute(prompt, context);

            String combinedPlan = "[Meta-Orchestrator Intent Decomposition]\n" +
                    "- Classified intent to domain specialist: " + selectedSpecialist + "\n" +
                    "- Specialist Execution Plan:\n" + subResult.getPlan();

            String combinedSynthesis = "### Supervisory Meta-Orchestrator Overview\n" +
                    "> **Delegated To:** `" + selectedSpecialist.name() + "`\n\n" +
                    subResult.getFinalSynthesis();

            return AgentExecutionResult.builder()
                    .plan(combinedPlan)
                    .toolCalls(subResult.getToolCalls())
                    .finalSynthesis(combinedSynthesis)
                    .confidenceScore(subResult.getConfidenceScore())
                    .requiresHumanApproval(subResult.isRequiresHumanApproval())
                    .approvalReason(subResult.getApprovalReason())
                    .actionToApprove(subResult.getActionToApprove())
                    .build();
        }

        // Fallback default response
        return AgentExecutionResult.builder()
                .plan("Direct meta-orchestration analysis.")
                .toolCalls(Collections.emptyList())
                .finalSynthesis("Completed supervisory evaluation for: " + prompt)
                .confidenceScore(0.95)
                .requiresHumanApproval(false)
                .build();
    }

    public AgentType routeIntent(String prompt) {
        if (prompt == null) return AgentType.REPORT_SYNTHESIZER_SPECIALIST;
        String lower = prompt.toLowerCase();

        if (lower.contains("fraud") || lower.contains("aml") || lower.contains("freeze") || lower.contains("suspicious")) {
            return AgentType.FRAUD_DETECTION_SPECIALIST;
        } else if (lower.contains("reconcil") || lower.contains("match") || lower.contains("discrepan") || lower.contains("write off") || lower.contains("write-off")) {
            return AgentType.RECONCILIATION_SPECIALIST;
        } else if (lower.contains("expense") || lower.contains("spend") || lower.contains("vendor") || lower.contains("cost cut")) {
            return AgentType.EXPENSE_OPTIMIZATION_ADVISOR;
        } else if (lower.contains("invoice") || lower.contains("ar aging") || lower.contains("receivable") || lower.contains("dunning") || lower.contains("overdue")) {
            return AgentType.INVOICE_AR_COLLECTIONS_SPECIALIST;
        } else if (lower.contains("cash flow") || lower.contains("forecast") || lower.contains("runway") || lower.contains("burn rate")) {
            return AgentType.CASH_FLOW_FORECAST_SPECIALIST;
        } else if (lower.contains("audit") || lower.contains("compliance") || lower.contains("trail") || lower.contains("sox") || lower.contains("investigat")) {
            return AgentType.AUDIT_COMPLIANCE_INVESTIGATOR;
        } else if (lower.contains("executive") || lower.contains("board") || lower.contains("brief") || lower.contains("summary") || lower.contains("overview")) {
            return AgentType.REPORT_SYNTHESIZER_SPECIALIST;
        } else {
            return AgentType.FINANCIAL_DATA_ANALYST;
        }
    }
}
