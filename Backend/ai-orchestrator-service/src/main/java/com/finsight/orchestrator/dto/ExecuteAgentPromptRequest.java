package com.finsight.orchestrator.dto;

import com.finsight.orchestrator.enums.AgentType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteAgentPromptRequest {

    private AgentType agentType;

    @NotBlank(message = "Prompt cannot be blank")
    private String prompt;

    private Map<String, Object> parameters;
}
