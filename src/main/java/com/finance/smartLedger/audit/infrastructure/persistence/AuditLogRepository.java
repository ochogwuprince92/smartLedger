package com.finance.smartLedger.audit.infrastructure.persistence;

import com.finance.smartLedger.audit.domain.AuditAction;
import com.finance.smartLedger.audit.domain.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository
    extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

  @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType")
  List<AuditLog> findByEntityType(@Param("entityType") String entityType);

  @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId")
  List<AuditLog> findByEntityTypeAndEntityId(
      @Param("entityType") String entityType, @Param("entityId") UUID entityId);

  @Query("SELECT a FROM AuditLog a WHERE a.action = :action")
  List<AuditLog> findByAction(@Param("action") AuditAction action);

  @Query("SELECT a FROM AuditLog a WHERE a.createdBy = :createdBy")
  List<AuditLog> findByCreatedBy(@Param("createdBy") String createdBy);

  @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate")
  List<AuditLog> findByCreatedAtBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.action = :action")
  List<AuditLog> findByEntityTypeAndAction(
      @Param("entityType") String entityType, @Param("action") AuditAction action);

  @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress")
  List<AuditLog> findByIpAddress(@Param("ipAddress") String ipAddress);

  @Query("SELECT a FROM AuditLog a WHERE a.sessionId = :sessionId")
  List<AuditLog> findBySessionId(@Param("sessionId") String sessionId);

  @Query("SELECT a FROM AuditLog a WHERE a.requestId = :requestId")
  List<AuditLog> findByRequestId(@Param("requestId") String requestId);

  @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType ORDER BY a.createdAt DESC")
  List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(@Param("entityType") String entityType);

  @Query(
      "SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.createdAt DESC")
  List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
      @Param("entityType") String entityType, @Param("entityId") UUID entityId);
}
