package com.finance.smartLedger.audit.application.dto;

import com.finance.smartLedger.audit.domain.AuditAction;
import com.finance.smartLedger.audit.domain.AuditLog;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response for an audit log")
public record AuditLogResponse(
    @Schema(description = "Audit log ID") UUID id,
    @Schema(description = "Entity type") String entityType,
    @Schema(description = "Entity ID") UUID entityId,
    @Schema(description = "Action") AuditAction action,
    @Schema(description = "Description") String description,
    @Schema(description = "Old value") String oldValue,
    @Schema(description = "New value") String newValue,
    @Schema(description = "Changed fields") String changedFields,
    @Schema(description = "IP address") String ipAddress,
    @Schema(description = "User agent") String userAgent,
    @Schema(description = "Request ID") String requestId,
    @Schema(description = "Session ID") String sessionId,
    @Schema(description = "Metadata") String metadata,
    @Schema(description = "Created at") LocalDateTime createdAt,
    @Schema(description = "Updated at") LocalDateTime updatedAt,
    @Schema(description = "Created by") String createdBy,
    @Schema(description = "Updated by") String updatedBy) {

  public static AuditLogResponse from(AuditLog auditLog) {
    return new AuditLogResponse(
        auditLog.getId(),
        auditLog.getEntityType(),
        auditLog.getEntityId(),
        auditLog.getAction(),
        auditLog.getDescription(),
        auditLog.getOldValue(),
        auditLog.getNewValue(),
        auditLog.getChangedFields(),
        auditLog.getIpAddress(),
        auditLog.getUserAgent(),
        auditLog.getRequestId(),
        auditLog.getSessionId(),
        auditLog.getMetadata(),
        auditLog.getCreatedAt(),
        auditLog.getUpdatedAt(),
        auditLog.getCreatedBy(),
        auditLog.getUpdatedBy());
  }
}
