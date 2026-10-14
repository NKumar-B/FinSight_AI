package com.finsight.financial.repository;

import com.finsight.financial.entity.ImportBatch;
import com.finsight.financial.enums.ImportBatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {
    Optional<ImportBatch> findByBatchReference(String batchReference);
    Optional<ImportBatch> findByFileChecksumSha256(String fileChecksumSha256);
    boolean existsByFileChecksumSha256AndStatus(String fileChecksumSha256, ImportBatchStatus status);
}
