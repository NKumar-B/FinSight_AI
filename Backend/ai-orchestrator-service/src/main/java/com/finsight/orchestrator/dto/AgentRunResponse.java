package com.finsight.orchestrator.dto;

import com.finsight.orchestrator.enums.AgentType;
import com.finsight.orchestrator.enums.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRunResponse {

    private Long id;
    private String runReference;
    private AgentType agentType;
    private String userPrompt;
    private String plan;
    private Object toolCallsHistory;
    private String finalSynthesis;
    private double confidenceScore;
    private ExecutionStatus status;
    private boolean requiresHumanApproval;
    private String approvalReason;
    private String actionToApprove;
    private String reviewedBy;
    private String reviewNotes;
    private String initiatedBy;
    private Instant createdAt;
    private Instant completedAt;
}
