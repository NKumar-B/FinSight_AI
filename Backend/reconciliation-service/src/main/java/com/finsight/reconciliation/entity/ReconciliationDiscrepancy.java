package com.finsight.reconciliation.entity;

import com.finsight.reconciliation.enums.CaseStatus;
import com.finsight.reconciliation.enums.DiscrepancyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "reconciliation_discrepancies", indexes = {
        @Index(name = "idx_case_reference", columnList = "case_reference"),
        @Index(name = "idx_case_run_id", columnList = "run_id"),
        @Index(name = "idx_case_status", columnList = "status"),
        @Index(name = "idx_case_type", columnList = "discrepancy_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationDiscrepancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private ReconciliationRun reconciliationRun;

    @Column(name = "case_reference", nullable = false, unique = true, length = 64)
    private String caseReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "discrepancy_type", nullable = false, length = 40)
    private DiscrepancyType discrepancyType;

    @Column(name = "source_transaction_id")
    private Long sourceTransactionId;

    @Column(name = "source_transaction_ref", length = 100)
    private String sourceTransactionRef;

    @Column(name = "target_transaction_id")
    private Long targetTransactionId;

    @Column(name = "target_transaction_ref", length = 100)
    private String targetTransactionRef;

    @Column(name = "source_amount", precision = 18, scale = 2)
    private BigDecimal sourceAmount;

    @Column(name = "target_amount", precision = 18, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "amount_discrepancy", precision = 18, scale = 2)
    private BigDecimal amountDiscrepancy;

    @Column(name = "source_date")
    private LocalDate sourceDate;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CaseStatus status = CaseStatus.OPEN;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
