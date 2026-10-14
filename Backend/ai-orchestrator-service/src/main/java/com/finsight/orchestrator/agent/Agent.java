package com.finsight.orchestrator.agent;

import com.finsight.orchestrator.enums.AgentType;

import java.util.Map;

public interface Agent {
    AgentType getType();
    String getDescription();
    AgentExecutionResult execute(String prompt, Map<String, Object> context);
}
