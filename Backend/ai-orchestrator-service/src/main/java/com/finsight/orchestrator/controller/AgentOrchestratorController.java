package com.finsight.orchestrator.controller;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
import com.finsight.orchestrator.dto.AgentRunResponse;
import com.finsight.orchestrator.dto.AvailableToolDto;
import com.finsight.orchestrator.dto.ExecuteAgentPromptRequest;
import com.finsight.orchestrator.dto.HumanApprovalRequest;
import com.finsight.orchestrator.enums.AgentType;
import com.finsight.orchestrator.enums.ExecutionStatus;
import com.finsight.orchestrator.service.AgentOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Orchestration API", description = "Endpoints for AI agent prompt execution, tool orchestration, and human approval review gates")
public class AgentOrchestratorController {

    private final AgentOrchestrationService agentOrchestrationService;

    @PostMapping("/execute")
    @Operation(summary = "Execute prompt through AI specialist agents", description = "Dispatches natural language prompt to supervisory meta-orchestrator or designated specialist agent")
    public ResponseEntity<ApiResponse<AgentRunResponse>> executePrompt(
            @Valid @RequestBody ExecuteAgentPromptRequest request,
            @AuthenticationPrincipal String principal) {
        String initiatedBy = principal != null ? principal : "API_USER";
        AgentRunResponse response = agentOrchestrationService.executePrompt(request, initiatedBy);
        return ResponseEntity.ok(ApiResponse.ok(response, "Agent execution initiated successfully"));
    }

    @PostMapping("/runs/{runReference}/review")
    @Operation(summary = "Submit human approval review decision", description = "Approves or rejects a gated high-impact action requiring human sign-off")
    public ResponseEntity<ApiResponse<AgentRunResponse>> reviewRun(
            @PathVariable("runReference") String runReference,
            @Valid @RequestBody HumanApprovalRequest request,
            @AuthenticationPrincipal String principal) {
        String reviewedBy = principal != null ? principal : "COMPLIANCE_OFFICER";
        AgentRunResponse response = agentOrchestrationService.reviewRun(runReference, request, reviewedBy);
        return ResponseEntity.ok(ApiResponse.ok(response, "Human review submitted successfully"));
    }

    @GetMapping("/runs/{runReference}")
    @Operation(summary = "Get agent run by reference", description = "Retrieves execution details, tool invocations, and synthesized findings")
    public ResponseEntity<ApiResponse<AgentRunResponse>> getRun(
            @PathVariable("runReference") String runReference) {
        AgentRunResponse response = agentOrchestrationService.getRun(runReference);
        return ResponseEntity.ok(ApiResponse.ok(response, "Agent run retrieved successfully"));
    }

    @GetMapping("/runs")
    @Operation(summary = "List historical agent runs", description = "Retrieves paginated agent runs with optional status or agentType filters")
    public ResponseEntity<ApiResponse<PagedResponse<AgentRunResponse>>> getRuns(
            @RequestParam(name = "status", required = false) ExecutionStatus status,
            @RequestParam(name = "agentType", required = false) AgentType agentType,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<AgentRunResponse> runs = agentOrchestrationService.getRuns(status, agentType, pageRequest);
        return ResponseEntity.ok(ApiResponse.ok(runs, "Agent runs retrieved successfully"));
    }

    @GetMapping("/tools")
    @Operation(summary = "List registered orchestrator tools", description = "Retrieves all callable microservice tools available to the specialist agents")
    public ResponseEntity<ApiResponse<List<AvailableToolDto>>> getTools() {
        List<AvailableToolDto> tools = agentOrchestrationService.getAvailableTools();
        return ResponseEntity.ok(ApiResponse.ok(tools, "Available tools retrieved successfully"));
    }

    @GetMapping("/agents")
    @Operation(summary = "List specialist AI agents", description = "Retrieves metadata and descriptions for all 9 specialized financial AI agents")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAgents() {
        List<Map<String, Object>> agents = agentOrchestrationService.getAvailableAgents();
        return ResponseEntity.ok(ApiResponse.ok(agents, "Available agents retrieved successfully"));
    }
}
