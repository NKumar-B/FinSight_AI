package com.finsight.audit.repository;

import com.finsight.audit.entity.ComplianceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComplianceReportRepository extends JpaRepository<ComplianceReport, Long> {
    Optional<ComplianceReport> findByReportReference(String reportReference);
}
