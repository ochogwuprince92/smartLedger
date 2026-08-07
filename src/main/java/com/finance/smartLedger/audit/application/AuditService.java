package com.finance.smartLedger.audit.application;

import com.finance.smartLedger.audit.domain.AuditAction;
import com.finance.smartLedger.audit.domain.AuditLog;
import com.finance.smartLedger.audit.infrastructure.persistence.AuditLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

  private final AuditLogRepository auditLogRepository;

  @Transactional
  public AuditLog logAction(
      String entityType,
      UUID entityId,
      AuditAction action,
      String description,
      String oldValue,
      String newValue,
      String changedFields,
      String ipAddress,
      String userAgent,
      String requestId,
      String sessionId,
      String createdBy) {
    AuditLog auditLog =
        new AuditLog(
            entityType,
            entityId,
            action,
            description,
            oldValue,
            newValue,
            changedFields,
            ipAddress,
            userAgent,
            requestId,
            sessionId,
            createdBy);

    return auditLogRepository.save(auditLog);
  }

  @Transactional
  public AuditLog logCreate(
      String entityType, UUID entityId, String description, String newValue, String createdBy) {
    return logAction(
        entityType,
        entityId,
        AuditAction.CREATE,
        description,
        null,
        newValue,
        null,
        null,
        null,
        null,
        null,
        createdBy);
  }

  @Transactional
  public AuditLog logUpdate(
      String entityType,
      UUID entityId,
      String description,
      String oldValue,
      String newValue,
      String changedFields,
      String createdBy) {
    return logAction(
        entityType,
        entityId,
        AuditAction.UPDATE,
        description,
        oldValue,
        newValue,
        changedFields,
        null,
        null,
        null,
        null,
        createdBy);
  }

  @Transactional
  public AuditLog logDelete(
      String entityType, UUID entityId, String description, String oldValue, String createdBy) {
    return logAction(
        entityType,
        entityId,
        AuditAction.DELETE,
        description,
        oldValue,
        null,
        null,
        null,
        null,
        null,
        null,
        createdBy);
  }

  @Transactional
  public AuditLog logStatusChange(
      String entityType,
      UUID entityId,
      String description,
      String oldValue,
      String newValue,
      String createdBy) {
    return logAction(
        entityType,
        entityId,
        AuditAction.STATUS_CHANGE,
        description,
        oldValue,
        newValue,
        "status",
        null,
        null,
        null,
        null,
        createdBy);
  }

  public List<AuditLog> findByEntityType(String entityType) {
    return auditLogRepository.findByEntityType(entityType);
  }

  public List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId) {
    return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
  }

  public List<AuditLog> findByAction(AuditAction action) {
    return auditLogRepository.findByAction(action);
  }

  public List<AuditLog> findByCreatedBy(String createdBy) {
    return auditLogRepository.findByCreatedBy(createdBy);
  }

  public List<AuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return auditLogRepository.findByCreatedAtBetween(startDate, endDate);
  }

  public List<AuditLog> findByEntityTypeAndAction(String entityType, AuditAction action) {
    return auditLogRepository.findByEntityTypeAndAction(entityType, action);
  }

  public List<AuditLog> findByIpAddress(String ipAddress) {
    return auditLogRepository.findByIpAddress(ipAddress);
  }

  public List<AuditLog> findBySessionId(String sessionId) {
    return auditLogRepository.findBySessionId(sessionId);
  }

  public List<AuditLog> findByRequestId(String requestId) {
    return auditLogRepository.findByRequestId(requestId);
  }

  public List<AuditLog> findEntityHistory(String entityType, UUID entityId) {
    return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
  }

  public java.util.Optional<AuditLog> getAuditLog(UUID id) {
    return auditLogRepository.findById(id);
  }

  @Transactional
  public void deleteAuditLog(UUID id) {
    auditLogRepository.deleteById(id);
  }

  @Transactional
  public void deleteAuditLogsBeforeDate(LocalDateTime date) {
    List<AuditLog> oldLogs = auditLogRepository.findByCreatedAtBetween(LocalDateTime.MIN, date);
    auditLogRepository.deleteAll(oldLogs);
  }
}
