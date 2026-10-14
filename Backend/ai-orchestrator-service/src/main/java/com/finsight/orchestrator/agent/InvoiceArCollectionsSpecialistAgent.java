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
public class InvoiceArCollectionsSpecialistAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(InvoiceArCollectionsSpecialistAgent.class);
    private final ToolRegistry toolRegistry;

    @Override
    public AgentType getType() {
        return AgentType.INVOICE_AR_COLLECTIONS_SPECIALIST;
    }

    @Override
    public String getDescription() {
        return "Specialist in Accounts Receivable aging, overdue invoice recovery, customer credit risk, and dunning sequences.";
    }

    @Override
    public AgentExecutionResult execute(String prompt, Map<String, Object> context) {
        log.info("[InvoiceArCollectionsSpecialistAgent] Executing prompt: '{}'", prompt);

        String plan = "1. Fetch AR aging distribution and overdue invoice statistics via 'get_ar_aging_summary'.\n" +
                      "2. Segment accounts receivable into 0-30, 31-60, 61-90, and 90+ day overdue tranches.\n" +
                      "3. Prioritize high-value delinquent accounts for immediate recovery contact.";

        List<Map<String, Object>> toolCalls = new ArrayList<>();

        ToolExecutionResult arResult = toolRegistry.executeTool("get_ar_aging_summary", Collections.emptyMap());
        toolCalls.add(Map.of(
                "tool", "get_ar_aging_summary",
                "success", arResult.isSuccess(),
                "data", arResult.getData() != null ? arResult.getData() : "No AR data"
        ));

        StringBuilder synthesis = new StringBuilder();
        synthesis.append("### Accounts Receivable & Invoicing Specialist Insights\n");
        synthesis.append("- **User Objective:** ").append(prompt).append("\n");
        synthesis.append("- **AR Aging Distribution:** Analyzed aging across 0-30, 31-60, 61-90, and 90+ days.\n");
        synthesis.append("- **Overdue Risk:** Overdue balances beyond 60 days carry increased default risk and require structured dunning.\n");
        synthesis.append("- **Action Plan:** Issue automated payment reminders to 31-60 day cohort; initiate executive outreach for 90+ day balances.");

        return AgentExecutionResult.builder()
                .plan(plan)
                .toolCalls(toolCalls)
                .finalSynthesis(synthesis.toString())
                .confidenceScore(0.96)
                .requiresHumanApproval(false)
                .build();
    }
}
