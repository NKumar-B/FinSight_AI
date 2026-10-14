package com.finsight.orchestrator.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecutionResult {
    private String plan;
    private List<Map<String, Object>> toolCalls;
    private String finalSynthesis;
    @Builder.Default
    private double confidenceScore = 0.95;
    @Builder.Default
    private boolean requiresHumanApproval = false;
    private String approvalReason;
    private String actionToApprove;
}
