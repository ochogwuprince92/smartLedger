package com.finance.smartLedger.audit.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends AuditableEntity {

  @Column(name = "entity_type", nullable = false, length = 50)
  private String entityType;

  @Column(name = "entity_id")
  private UUID entityId;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", nullable = false, length = 30)
  private AuditAction action;

  @Column(name = "description", length = 500)
  private String description;

  @Column(name = "old_value", columnDefinition = "TEXT")
  private String oldValue;

  @Column(name = "new_value", columnDefinition = "TEXT")
  private String newValue;

  @Column(name = "changed_fields", columnDefinition = "TEXT")
  private String changedFields;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  @Column(name = "request_id", length = 100)
  private String requestId;

  @Column(name = "session_id", length = 100)
  private String sessionId;

  @Column(name = "metadata", columnDefinition = "TEXT")
  private String metadata;

  public AuditLog(
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
    this.entityType = entityType;
    this.entityId = entityId;
    this.action = action;
    this.description = description;
    this.oldValue = oldValue;
    this.newValue = newValue;
    this.changedFields = changedFields;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.requestId = requestId;
    this.sessionId = sessionId;
    this.setCreatedBy(createdBy);
    this.setUpdatedBy(createdBy);
  }
}
