package com.finsight.orchestrator.entity;

import com.finsight.orchestrator.enums.AgentType;
import com.finsight.orchestrator.enums.ExecutionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "agent_runs", indexes = {
        @Index(name = "idx_agent_run_ref", columnList = "run_reference"),
        @Index(name = "idx_agent_type", columnList = "agent_type"),
        @Index(name = "idx_agent_status", columnList = "status"),
        @Index(name = "idx_agent_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_reference", nullable = false, unique = true, length = 64)
    private String runReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", nullable = false, length = 50)
    private AgentType agentType;

    @Column(name = "user_prompt", nullable = false, columnDefinition = "TEXT")
    private String userPrompt;

    @Column(name = "plan", columnDefinition = "TEXT")
    private String plan;

    @Column(name = "tool_calls_history", columnDefinition = "TEXT")
    private String toolCallsHistory;

    @Column(name = "final_synthesis", columnDefinition = "TEXT")
    private String finalSynthesis;

    @Builder.Default
    @Column(name = "confidence_score", nullable = false)
    private double confidenceScore = 0.95;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ExecutionStatus status = ExecutionStatus.PENDING;

    @Builder.Default
    @Column(name = "requires_human_approval", nullable = false)
    private boolean requiresHumanApproval = false;

    @Column(name = "approval_reason", length = 500)
    private String approvalReason;

    @Column(name = "action_to_approve", columnDefinition = "TEXT")
    private String actionToApprove;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    @Column(name = "initiated_by", length = 100)
    private String initiatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
