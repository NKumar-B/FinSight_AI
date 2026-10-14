package com.finsight.orchestrator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.orchestrator.client.*;
import com.finsight.orchestrator.dto.AgentRunResponse;
import com.finsight.orchestrator.dto.AvailableToolDto;
import com.finsight.orchestrator.dto.ExecuteAgentPromptRequest;
import com.finsight.orchestrator.dto.HumanApprovalRequest;
import com.finsight.orchestrator.enums.AgentType;
import com.finsight.orchestrator.enums.ExecutionStatus;
import com.finsight.orchestrator.service.AgentOrchestrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgentOrchestratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgentOrchestrationService agentOrchestrationService;

    @MockBean
    private FinancialDataClient financialDataClient;

    @MockBean
    private ReconciliationClient reconciliationClient;

    @MockBean
    private FraudDetectionClient fraudDetectionClient;

    @MockBean
    private FinancialInsightsClient financialInsightsClient;

    @MockBean
    private AuditClient auditClient;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("POST /api/v1/ai/execute - Should execute prompt and return run response")
    void shouldExecutePrompt() throws Exception {
        ExecuteAgentPromptRequest request = ExecuteAgentPromptRequest.builder()
                .agentType(AgentType.FINANCIAL_DATA_ANALYST)
                .prompt("Summarize general ledger status")
                .build();

        AgentRunResponse response = AgentRunResponse.builder()
                .id(1L)
                .runReference("RUN-ABC123")
                .agentType(AgentType.FINANCIAL_DATA_ANALYST)
                .userPrompt("Summarize general ledger status")
                .status(ExecutionStatus.COMPLETED)
                .confidenceScore(0.96)
                .finalSynthesis("Synthesis results")
                .createdAt(Instant.now())
                .build();

        when(agentOrchestrationService.executePrompt(any(ExecuteAgentPromptRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/ai/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.runReference").value("RUN-ABC123"))
                .andExpect(jsonPath("$.data.agentType").value("FINANCIAL_DATA_ANALYST"));
    }

    @Test
    @WithMockUser(username = "compliance_mgr", roles = {"COMPLIANCE_OFFICER"})
    @DisplayName("POST /api/v1/ai/runs/{runReference}/review - Should submit human approval decision")
    void shouldSubmitHumanReview() throws Exception {
        HumanApprovalRequest request = HumanApprovalRequest.builder()
                .approved(true)
                .reviewNotes("Approved by Compliance Manager")
                .build();

        AgentRunResponse response = AgentRunResponse.builder()
                .id(2L)
                .runReference("RUN-GATE-999")
                .status(ExecutionStatus.APPROVED)
                .reviewedBy("compliance_mgr")
                .reviewNotes("Approved by Compliance Manager")
                .build();

        when(agentOrchestrationService.reviewRun(eq("RUN-GATE-999"), any(HumanApprovalRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/ai/runs/RUN-GATE-999/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.reviewedBy").value("compliance_mgr"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("GET /api/v1/ai/tools - Should return registered microservice tools")
    void shouldReturnTools() throws Exception {
        when(agentOrchestrationService.getAvailableTools()).thenReturn(List.of(
                AvailableToolDto.builder().name("fetch_account_summary").category("FINANCIAL_DATA").description("Accounts").build()
        ));

        mockMvc.perform(get("/api/v1/ai/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("fetch_account_summary"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("GET /api/v1/ai/agents - Should return all 9 specialist agents")
    void shouldReturnAgents() throws Exception {
        when(agentOrchestrationService.getAvailableAgents()).thenReturn(List.of(
                Map.of("agentType", "SUPERVISORY_META_ORCHESTRATOR", "name", "SUPERVISORY META ORCHESTRATOR")
        ));

        mockMvc.perform(get("/api/v1/ai/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].agentType").value("SUPERVISORY_META_ORCHESTRATOR"));
    }
}
