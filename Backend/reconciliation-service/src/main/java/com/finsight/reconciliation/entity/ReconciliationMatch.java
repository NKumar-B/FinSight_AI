package com.finsight.reconciliation.entity;

import com.finsight.reconciliation.enums.MatchStatus;
import com.finsight.reconciliation.enums.MatchType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reconciliation_matches", indexes = {
        @Index(name = "idx_match_run_id", columnList = "run_id"),
        @Index(name = "idx_match_source_id", columnList = "source_transaction_id"),
        @Index(name = "idx_match_target_id", columnList = "target_transaction_id"),
        @Index(name = "idx_match_type", columnList = "match_type"),
        @Index(name = "idx_match_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private ReconciliationRun reconciliationRun;

    @Column(name = "source_transaction_id", nullable = false)
    private Long sourceTransactionId;

    @Column(name = "source_transaction_ref", nullable = false, length = 100)
    private String sourceTransactionRef;

    @Column(name = "target_transaction_id")
    private Long targetTransactionId;

    @Column(name = "target_transaction_ref", length = 100)
    private String targetTransactionRef;

    @Column(name = "target_transaction_ids", length = 500)
    private String targetTransactionIds;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 30)
    private MatchType matchType;

    @Builder.Default
    @Column(name = "amount_difference", nullable = false, precision = 18, scale = 2)
    private BigDecimal amountDifference = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "date_difference_days", nullable = false)
    private int dateDifferenceDays = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchStatus status = MatchStatus.AUTOMATICALLY_MATCHED;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "review_notes", length = 500)
    private String reviewNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
