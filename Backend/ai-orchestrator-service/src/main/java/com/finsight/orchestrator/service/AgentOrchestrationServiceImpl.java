package com.finsight.orchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.exception.ConflictException;
import com.finsight.common.exception.ResourceNotFoundException;
import com.finsight.orchestrator.agent.Agent;
import com.finsight.orchestrator.agent.AgentExecutionResult;
import com.finsight.orchestrator.dto.AgentRunResponse;
import com.finsight.orchestrator.dto.AvailableToolDto;
import com.finsight.orchestrator.dto.ExecuteAgentPromptRequest;
import com.finsight.orchestrator.dto.HumanApprovalRequest;
import com.finsight.orchestrator.entity.AgentRun;
import com.finsight.orchestrator.enums.AgentType;
import com.finsight.orchestrator.enums.ExecutionStatus;
import com.finsight.orchestrator.mapper.AgentRunMapper;
import com.finsight.orchestrator.repository.AgentRunRepository;
import com.finsight.orchestrator.tools.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgentOrchestrationServiceImpl implements AgentOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrationServiceImpl.class);

    private final AgentRunRepository agentRunRepository;
    private final AgentRunMapper agentRunMapper;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;
    private final Map<AgentType, Agent> agentMap = new EnumMap<>(AgentType.class);

    public AgentOrchestrationServiceImpl(
            AgentRunRepository agentRunRepository,
            AgentRunMapper agentRunMapper,
            ToolRegistry toolRegistry,
            ObjectMapper objectMapper,
            List<Agent> agents) {
        this.agentRunRepository = agentRunRepository;
        this.agentRunMapper = agentRunMapper;
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
        for (Agent a : agents) {
            this.agentMap.put(a.getType(), a);
        }
    }

    @Override
    @Transactional
    public AgentRunResponse executePrompt(ExecuteAgentPromptRequest request, String initiatedBy) {
        log.info("[AgentOrchestrationService] Initiating execution for prompt: '{}' by user: {}", request.getPrompt(), initiatedBy);

        AgentType targetType = request.getAgentType() != null
                ? request.getAgentType()
                : AgentType.SUPERVISORY_META_ORCHESTRATOR;

        Agent agent = agentMap.get(targetType);
        if (agent == null) {
            throw new ResourceNotFoundException("Agent", "type", targetType.name());
        }

        String runReference = "RUN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        AgentExecutionResult result = agent.execute(request.getPrompt(), request.getParameters());

        String toolHistoryJson = "[]";
        try {
            toolHistoryJson = objectMapper.writeValueAsString(result.getToolCalls());
        } catch (Exception e) {
            log.warn("Failed to serialize tool history: {}", e.getMessage());
        }

        ExecutionStatus status = result.isRequiresHumanApproval()
                ? ExecutionStatus.AWAITING_HUMAN_APPROVAL
                : ExecutionStatus.COMPLETED;

        Instant completedAt = result.isRequiresHumanApproval() ? null : Instant.now();

        AgentRun run = AgentRun.builder()
                .runReference(runReference)
                .agentType(targetType)
                .userPrompt(request.getPrompt())
                .plan(result.getPlan())
                .toolCallsHistory(toolHistoryJson)
                .finalSynthesis(result.getFinalSynthesis())
                .confidenceScore(result.getConfidenceScore())
                .status(status)
                .requiresHumanApproval(result.isRequiresHumanApproval())
                .approvalReason(result.getApprovalReason())
                .actionToApprove(result.getActionToApprove())
                .initiatedBy(initiatedBy != null ? initiatedBy : "SYSTEM")
                .completedAt(completedAt)
                .build();

        AgentRun saved = agentRunRepository.save(run);
        log.info("[AgentOrchestrationService] Run persisted with reference: {} and status: {}", saved.getRunReference(), saved.getStatus());
        return agentRunMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AgentRunResponse reviewRun(String runReference, HumanApprovalRequest request, String reviewedBy) {
        log.info("[AgentOrchestrationService] Reviewing run reference: {} by: {}", runReference, reviewedBy);

        AgentRun run = agentRunRepository.findByRunReference(runReference)
                .orElseThrow(() -> new ResourceNotFoundException("AgentRun", "runReference", runReference));

        if (run.getStatus() != ExecutionStatus.AWAITING_HUMAN_APPROVAL) {
            throw new ConflictException("Agent run is not awaiting human approval. Current status: " + run.getStatus());
        }

        run.setStatus(request.isApproved() ? ExecutionStatus.APPROVED : ExecutionStatus.REJECTED);
        run.setReviewedBy(reviewedBy != null ? reviewedBy : "SUPERVISOR");
        run.setReviewNotes(request.getReviewNotes());
        run.setCompletedAt(Instant.now());

        AgentRun updated = agentRunRepository.save(run);
        log.info("[AgentOrchestrationService] Run {} review complete: status={}", runReference, updated.getStatus());
        return agentRunMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public AgentRunResponse getRun(String runReference) {
        AgentRun run = agentRunRepository.findByRunReference(runReference)
                .orElseThrow(() -> new ResourceNotFoundException("AgentRun", "runReference", runReference));
        return agentRunMapper.toResponse(run);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AgentRunResponse> getRuns(ExecutionStatus status, AgentType agentType, Pageable pageable) {
        Page<AgentRun> page;
        if (status != null) {
            page = agentRunRepository.findByStatus(status, pageable);
        } else if (agentType != null) {
            page = agentRunRepository.findByAgentType(agentType, pageable);
        } else {
            page = agentRunRepository.findAll(pageable);
        }

        List<AgentRunResponse> content = page.getContent().stream()
                .map(agentRunMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<AgentRunResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public List<AvailableToolDto> getAvailableTools() {
        return toolRegistry.listTools().stream()
                .map(t -> AvailableToolDto.builder()
                        .name(t.getName())
                        .description(t.getDescription())
                        .category(t.getCategory())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAvailableAgents() {
        return agentMap.values().stream()
                .map(a -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("agentType", a.getType().name());
                    map.put("name", a.getType().name().replace('_', ' '));
                    map.put("description", a.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
