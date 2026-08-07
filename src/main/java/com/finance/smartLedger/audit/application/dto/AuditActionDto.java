package com.finance.smartLedger.audit.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Audit action")
public enum AuditActionDto {
  CREATE,
  UPDATE,
  DELETE,
  STATUS_CHANGE,
  LOGIN,
  LOGOUT,
  EXPORT,
  IMPORT,
  BULK_OPERATION;

  @JsonCreator
  public static AuditActionDto fromString(String value) {
    return AuditActionDto.valueOf(value.toUpperCase());
  }

  public com.finance.smartLedger.audit.domain.AuditAction toDomain() {
    return com.finance.smartLedger.audit.domain.AuditAction.valueOf(name());
  }
}
