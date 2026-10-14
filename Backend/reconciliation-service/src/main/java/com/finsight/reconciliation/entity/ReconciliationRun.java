package com.finsight.reconciliation.entity;

import com.finsight.reconciliation.enums.ReconciliationRunStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reconciliation_runs", indexes = {
        @Index(name = "idx_recon_reference", columnList = "run_reference"),
        @Index(name = "idx_recon_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_reference", nullable = false, unique = true, length = 64)
    private String runReference;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReconciliationRunStatus status = ReconciliationRunStatus.IN_PROGRESS;

    @Builder.Default
    @Column(name = "total_source_records", nullable = false)
    private int totalSourceRecords = 0;

    @Builder.Default
    @Column(name = "total_target_records", nullable = false)
    private int totalTargetRecords = 0;

    @Builder.Default
    @Column(name = "exact_matches_count", nullable = false)
    private int exactMatchesCount = 0;

    @Builder.Default
    @Column(name = "tolerance_matches_count", nullable = false)
    private int toleranceMatchesCount = 0;

    @Builder.Default
    @Column(name = "discrepancies_count", nullable = false)
    private int discrepanciesCount = 0;

    @Builder.Default
    @Column(name = "unmatched_count", nullable = false)
    private int unmatchedCount = 0;

    @Builder.Default
    @Column(name = "date_tolerance_days", nullable = false)
    private int dateToleranceDays = 2;

    @Builder.Default
    @Column(name = "amount_tolerance", nullable = false, precision = 18, scale = 2)
    private BigDecimal amountTolerance = new BigDecimal("0.05");

    @Column(name = "executed_by", length = 100)
    private String executedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
