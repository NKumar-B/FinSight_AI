package com.finsight.orchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.orchestrator.agent.*;
import com.finsight.orchestrator.dto.AgentRunResponse;
import com.finsight.orchestrator.dto.ExecuteAgentPromptRequest;
import com.finsight.orchestrator.dto.HumanApprovalRequest;
import com.finsight.orchestrator.entity.AgentRun;
import com.finsight.orchestrator.enums.AgentType;
import com.finsight.orchestrator.enums.ExecutionStatus;
import com.finsight.orchestrator.mapper.AgentRunMapper;
import com.finsight.orchestrator.repository.AgentRunRepository;
import com.finsight.orchestrator.tools.ToolDefinition;
import com.finsight.orchestrator.tools.ToolExecutionResult;
import com.finsight.orchestrator.tools.ToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentOrchestrationServiceTest {

    @Mock
    private AgentRunRepository agentRunRepository;

    @Mock
    private ToolRegistry toolRegistry;

    private ObjectMapper objectMapper;
    private AgentRunMapper agentRunMapper;
    private AgentOrchestrationServiceImpl orchestrationService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        agentRunMapper = new AgentRunMapper(objectMapper);

        // Lenient mock toolRegistry behavior for agents
        lenient().when(toolRegistry.executeTool(anyString(), any())).thenReturn(
                ToolExecutionResult.builder().toolName("mock").success(true).data(Map.of("key", "val")).build()
        );

        // Instantiate agents
        FinancialDataAnalystAgent dataAgent = new FinancialDataAnalystAgent(toolRegistry);
        ReconciliationSpecialistAgent reconAgent = new ReconciliationSpecialistAgent(toolRegistry);
        FraudDetectionSpecialistAgent fraudAgent = new FraudDetectionSpecialistAgent(toolRegistry);
        ExpenseOptimizationAdvisorAgent expenseAgent = new ExpenseOptimizationAdvisorAgent(toolRegistry);
        InvoiceArCollectionsSpecialistAgent arAgent = new InvoiceArCollectionsSpecialistAgent(toolRegistry);
        CashFlowForecastSpecialistAgent cfAgent = new CashFlowForecastSpecialistAgent(toolRegistry);
        AuditComplianceInvestigatorAgent auditAgent = new AuditComplianceInvestigatorAgent(toolRegistry);
        ReportSynthesizerSpecialistAgent reportAgent = new ReportSynthesizerSpecialistAgent(toolRegistry);

        List<Agent> leafAgents = List.of(dataAgent, reconAgent, fraudAgent, expenseAgent, arAgent, cfAgent, auditAgent, reportAgent);
        SupervisoryMetaOrchestratorAgent metaAgent = new SupervisoryMetaOrchestratorAgent(leafAgents);

        List<Agent> allAgents = new ArrayList<>(leafAgents);
        allAgents.add(metaAgent);

        orchestrationService = new AgentOrchestrationServiceImpl(
                agentRunRepository,
                agentRunMapper,
                toolRegistry,
                objectMapper,
                allAgents
        );
    }

    @Test
    @DisplayName("Should execute prompt with supervisory routing to fraud specialist")
    void shouldExecutePromptRoutedToFraudSpecialist() {
        when(agentRunRepository.save(any(AgentRun.class))).thenAnswer(invocation -> {
            AgentRun run = invocation.getArgument(0);
            run.setId(1L);
            run.setCreatedAt(Instant.now());
            return run;
        });

        ExecuteAgentPromptRequest request = ExecuteAgentPromptRequest.builder()
                .prompt("Analyze recent AML alerts and suspicious high-amount transactions")
                .build();

        AgentRunResponse response = orchestrationService.executePrompt(request, "compliance_analyst");

        assertThat(response).isNotNull();
        assertThat(response.getRunReference()).startsWith("RUN-");
        assertThat(response.getStatus()).isEqualTo(ExecutionStatus.COMPLETED);
        assertThat(response.getFinalSynthesis()).contains("Fraud Detection Specialist");

        verify(agentRunRepository).save(any(AgentRun.class));
    }

    @Test
    @DisplayName("Should trigger human approval gate when prompt requests high impact write off")
    void shouldTriggerHumanApprovalGateOnWriteOff() {
        when(agentRunRepository.save(any(AgentRun.class))).thenAnswer(invocation -> {
            AgentRun run = invocation.getArgument(0);
            run.setId(2L);
            run.setCreatedAt(Instant.now());
            return run;
        });

        ExecuteAgentPromptRequest request = ExecuteAgentPromptRequest.builder()
                .agentType(AgentType.RECONCILIATION_SPECIALIST)
                .prompt("Approve write off for remaining unresolved balance discrepancy")
                .build();

        AgentRunResponse response = orchestrationService.executePrompt(request, "accountant_user");

        assertThat(response).isNotNull();
        assertThat(response.isRequiresHumanApproval()).isTrue();
        assertThat(response.getStatus()).isEqualTo(ExecutionStatus.AWAITING_HUMAN_APPROVAL);
        assertThat(response.getApprovalReason()).isNotBlank();
        assertThat(response.getActionToApprove()).contains("write-off");
    }

    @Test
    @DisplayName("Should approve a gated run during human review")
    void shouldApproveGatedRun() {
        AgentRun awaitingRun = AgentRun.builder()
                .id(10L)
                .runReference("RUN-TEST-001")
                .agentType(AgentType.RECONCILIATION_SPECIALIST)
                .userPrompt("Write off discrepancy")
                .status(ExecutionStatus.AWAITING_HUMAN_APPROVAL)
                .requiresHumanApproval(true)
                .approvalReason("Write-off requested")
                .createdAt(Instant.now().minusSeconds(300))
                .build();

        when(agentRunRepository.findByRunReference("RUN-TEST-001")).thenReturn(Optional.of(awaitingRun));
        when(agentRunRepository.save(any(AgentRun.class))).thenAnswer(i -> i.getArgument(0));

        HumanApprovalRequest approvalRequest = HumanApprovalRequest.builder()
                .approved(true)
                .reviewNotes("Verified by Chief Financial Officer")
                .build();

        AgentRunResponse response = orchestrationService.reviewRun("RUN-TEST-001", approvalRequest, "cfo_officer");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ExecutionStatus.APPROVED);
        assertThat(response.getReviewedBy()).isEqualTo("cfo_officer");
        assertThat(response.getReviewNotes()).isEqualTo("Verified by Chief Financial Officer");
        assertThat(response.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should list available tools and specialist agents")
    void shouldListToolsAndAgents() {
        when(toolRegistry.listTools()).thenReturn(List.of(
                ToolDefinition.builder().name("fetch_account_summary").description("Balances").category("FINANCIAL_DATA").build()
        ));

        var tools = orchestrationService.getAvailableTools();
        assertThat(tools).hasSize(1);
        assertThat(tools.get(0).getName()).isEqualTo("fetch_account_summary");

        var agents = orchestrationService.getAvailableAgents();
        assertThat(agents).hasSize(9);
    }
}
