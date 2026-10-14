package com.finsight.orchestrator.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.orchestrator.dto.AgentRunResponse;
import com.finsight.orchestrator.entity.AgentRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class AgentRunMapper {

    private final ObjectMapper objectMapper;

    public AgentRunResponse toResponse(AgentRun entity) {
        if (entity == null) return null;

        Object toolCallsObj = Collections.emptyList();
        if (entity.getToolCallsHistory() != null && !entity.getToolCallsHistory().isBlank()) {
            try {
                toolCallsObj = objectMapper.readValue(entity.getToolCallsHistory(), new TypeReference<>() {});
            } catch (Exception e) {
                toolCallsObj = entity.getToolCallsHistory();
            }
        }

        return AgentRunResponse.builder()
                .id(entity.getId())
                .runReference(entity.getRunReference())
                .agentType(entity.getAgentType())
                .userPrompt(entity.getUserPrompt())
                .plan(entity.getPlan())
                .toolCallsHistory(toolCallsObj)
                .finalSynthesis(entity.getFinalSynthesis())
                .confidenceScore(entity.getConfidenceScore())
                .status(entity.getStatus())
                .requiresHumanApproval(entity.isRequiresHumanApproval())
                .approvalReason(entity.getApprovalReason())
                .actionToApprove(entity.getActionToApprove())
                .reviewedBy(entity.getReviewedBy())
                .reviewNotes(entity.getReviewNotes())
                .initiatedBy(entity.getInitiatedBy())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
