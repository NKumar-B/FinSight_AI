package com.finsight.orchestrator.repository;

import com.finsight.orchestrator.entity.AgentRun;
import com.finsight.orchestrator.enums.AgentType;
import com.finsight.orchestrator.enums.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentRunRepository extends JpaRepository<AgentRun, Long> {

    Optional<AgentRun> findByRunReference(String runReference);

    Page<AgentRun> findByStatus(ExecutionStatus status, Pageable pageable);

    Page<AgentRun> findByAgentType(AgentType agentType, Pageable pageable);

    long countByStatus(ExecutionStatus status);
}
