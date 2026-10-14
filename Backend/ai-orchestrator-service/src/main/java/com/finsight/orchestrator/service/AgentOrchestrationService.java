package com.finsight.orchestrator.service;

import com.finsight.common.dto.PagedResponse;
import com.finsight.orchestrator.dto.AgentRunResponse;
import com.finsight.orchestrator.dto.AvailableToolDto;
import com.finsight.orchestrator.dto.ExecuteAgentPromptRequest;
import com.finsight.orchestrator.dto.HumanApprovalRequest;
import com.finsight.orchestrator.enums.AgentType;
import com.finsight.orchestrator.enums.ExecutionStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface AgentOrchestrationService {

    AgentRunResponse executePrompt(ExecuteAgentPromptRequest request, String initiatedBy);

    AgentRunResponse reviewRun(String runReference, HumanApprovalRequest request, String reviewedBy);

    AgentRunResponse getRun(String runReference);

    PagedResponse<AgentRunResponse> getRuns(ExecutionStatus status, AgentType agentType, Pageable pageable);

    List<AvailableToolDto> getAvailableTools();

    List<Map<String, Object>> getAvailableAgents();
}
