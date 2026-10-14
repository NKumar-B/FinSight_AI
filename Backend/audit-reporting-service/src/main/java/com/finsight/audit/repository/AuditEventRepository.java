package com.finsight.audit.repository;

import com.finsight.audit.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long>, JpaSpecificationExecutor<AuditEvent> {

    Page<AuditEvent> findByActorId(String actorId, Pageable pageable);

    Page<AuditEvent> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    Page<AuditEvent> findByCorrelationId(String correlationId, Pageable pageable);

    long countByOutcome(String outcome);

    @Query("SELECT e.actorType, COUNT(e) FROM AuditEvent e GROUP BY e.actorType")
    List<Object[]> countGroupedByActorType();

    @Query("SELECT e.action, COUNT(e) FROM AuditEvent e GROUP BY e.action ORDER BY COUNT(e) DESC")
    List<Object[]> countTopActions(Pageable pageable);
}
