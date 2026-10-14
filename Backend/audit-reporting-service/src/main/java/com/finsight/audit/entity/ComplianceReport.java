package com.finsight.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "compliance_reports", indexes = {
        @Index(name = "idx_report_reference", columnList = "report_reference"),
        @Index(name = "idx_report_type", columnList = "report_type"),
        @Index(name = "idx_report_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_reference", nullable = false, unique = true, length = 64)
    private String reportReference;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;

    @Column(name = "generated_by", nullable = false, length = 100)
    private String generatedBy;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    @Column(name = "total_events_count", nullable = false)
    private int totalEventsCount;

    @Column(name = "summary_content", columnDefinition = "TEXT")
    private String summaryContent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
