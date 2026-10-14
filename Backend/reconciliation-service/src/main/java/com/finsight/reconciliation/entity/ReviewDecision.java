package com.finsight.reconciliation.entity;

import com.finsight.reconciliation.enums.DecisionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "review_decisions", indexes = {
        @Index(name = "idx_decision_case_id", columnList = "case_id"),
        @Index(name = "idx_decision_match_id", columnList = "match_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "match_id")
    private Long matchId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DecisionType decision;

    @Column(name = "reviewer_id", nullable = false, length = 100)
    private String reviewerId;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "before_state", columnDefinition = "TEXT")
    private String beforeState;

    @Column(name = "after_state", columnDefinition = "TEXT")
    private String afterState;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
