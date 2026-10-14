package com.finsight.financial.entity;

import com.finsight.financial.enums.ImportBatchStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "import_batches", indexes = {
        @Index(name = "idx_batch_reference", columnList = "batch_reference"),
        @Index(name = "idx_batch_checksum", columnList = "file_checksum_sha256")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_reference", nullable = false, unique = true, length = 64)
    private String batchReference;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_checksum_sha256", nullable = false, length = 64)
    private String fileChecksumSha256;

    @Column(name = "account_id")
    private Long accountId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ImportBatchStatus status = ImportBatchStatus.PENDING;

    @Builder.Default
    @Column(name = "total_records", nullable = false)
    private int totalRecords = 0;

    @Builder.Default
    @Column(name = "successful_records", nullable = false)
    private int successfulRecords = 0;

    @Builder.Default
    @Column(name = "failed_records", nullable = false)
    private int failedRecords = 0;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @Column(name = "imported_by", length = 100)
    private String importedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
