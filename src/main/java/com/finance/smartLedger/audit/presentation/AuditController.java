package com.finance.smartLedger.audit.presentation;

import com.finance.smartLedger.audit.application.AuditService;
import com.finance.smartLedger.audit.application.dto.AuditActionDto;
import com.finance.smartLedger.audit.application.dto.AuditLogResponse;
import com.finance.smartLedger.audit.domain.AuditAction;
import com.finance.smartLedger.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit log management endpoints")
public class AuditController {

  private final AuditService auditService;

  @PostMapping("/logs")
  @Operation(summary = "Create audit log", description = "Creates a new audit log entry")
  @PreAuthorize("hasAuthority('AUDIT:CREATE')")
  public ResponseEntity<ApiResponse<AuditLogResponse>> createAuditLog(
      @RequestBody @Valid CreateAuditLogRequest request) {
    com.finance.smartLedger.audit.domain.AuditLog auditLog =
        auditService.logAction(
            request.entityType(),
            request.entityId(),
            request.action().toDomain(),
            request.description(),
            request.oldValue(),
            request.newValue(),
            request.changedFields(),
            request.ipAddress(),
            request.userAgent(),
            request.requestId(),
            request.sessionId(),
            request.createdBy());
    return ResponseEntity.ok(
        ApiResponse.success("Audit log created successfully", AuditLogResponse.from(auditLog)));
  }

  @GetMapping("/logs/{id}")
  @Operation(summary = "Get audit log by ID", description = "Retrieves an audit log by its ID")
  @PreAuthorize("hasAuthority('AUDIT:READ')")
  public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLog(
      @Parameter(description = "Audit log ID") @PathVariable UUID id) {
    com.finance.smartLedger.audit.domain.AuditLog auditLog =
        auditService
            .getAuditLog(id)
            .orElseThrow(() -> new IllegalArgumentException("Audit log not found"));
    return ResponseEntity.ok(
        ApiResponse.success("Audit log retrieved successfully", AuditLogResponse.from(auditLog)));
  }

  @GetMapping("/logs")
  @Operation(
      summary = "List audit logs",
      description = "Lists all audit logs with optional filters")
  @PreAuthorize("hasAuthority('AUDIT:READ')")
  public ResponseEntity<ApiResponse<List<AuditLogResponse>>> listAuditLogs(
      @Parameter(description = "Filter by entity type") @RequestParam(required = false)
          String entityType,
      @Parameter(description = "Filter by entity ID") @RequestParam(required = false) UUID entityId,
      @Parameter(description = "Filter by action") @RequestParam(required = false)
          AuditAction action,
      @Parameter(description = "Filter by created by") @RequestParam(required = false)
          String createdBy,
      @Parameter(description = "Filter by start date") @RequestParam(required = false)
          LocalDateTime startDate,
      @Parameter(description = "Filter by end date") @RequestParam(required = false)
          LocalDateTime endDate,
      @Parameter(description = "Filter by IP address") @RequestParam(required = false)
          String ipAddress,
      @Parameter(description = "Filter by session ID") @RequestParam(required = false)
          String sessionId,
      @Parameter(description = "Filter by request ID") @RequestParam(required = false)
          String requestId) {
    List<com.finance.smartLedger.audit.domain.AuditLog> auditLogs;

    if (entityType != null && entityId != null) {
      auditLogs = auditService.findByEntityTypeAndEntityId(entityType, entityId);
    } else if (entityType != null && action != null) {
      auditLogs = auditService.findByEntityTypeAndAction(entityType, action);
    } else if (entityType != null) {
      auditLogs = auditService.findByEntityType(entityType);
    } else if (action != null) {
      auditLogs = auditService.findByAction(action);
    } else if (createdBy != null) {
      auditLogs = auditService.findByCreatedBy(createdBy);
    } else if (ipAddress != null) {
      auditLogs = auditService.findByIpAddress(ipAddress);
    } else if (sessionId != null) {
      auditLogs = auditService.findBySessionId(sessionId);
    } else if (requestId != null) {
      auditLogs = auditService.findByRequestId(requestId);
    } else if (startDate != null && endDate != null) {
      auditLogs = auditService.findByCreatedAtBetween(startDate, endDate);
    } else {
      auditLogs = auditService.findByEntityType("Payment");
    }

    List<AuditLogResponse> responses =
        auditLogs.stream().map(AuditLogResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/logs/entity/{entityType}/{entityId}/history")
  @Operation(
      summary = "Get entity history",
      description = "Retrieves the complete audit history for an entity")
  @PreAuthorize("hasAuthority('AUDIT:READ')")
  public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getEntityHistory(
      @Parameter(description = "Entity type") @PathVariable String entityType,
      @Parameter(description = "Entity ID") @PathVariable UUID entityId) {
    List<com.finance.smartLedger.audit.domain.AuditLog> auditLogs =
        auditService.findEntityHistory(entityType, entityId);
    List<AuditLogResponse> responses =
        auditLogs.stream().map(AuditLogResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @DeleteMapping("/logs/{id}")
  @Operation(summary = "Delete audit log", description = "Deletes an audit log")
  @PreAuthorize("hasAuthority('AUDIT:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deleteAuditLog(
      @Parameter(description = "Audit log ID") @PathVariable UUID id) {
    auditService.deleteAuditLog(id);
    return ResponseEntity.ok(ApiResponse.success("Audit log deleted successfully", null));
  }

  @DeleteMapping("/logs/before-date")
  @Operation(
      summary = "Delete audit logs before date",
      description = "Deletes all audit logs before a specific date")
  @PreAuthorize("hasAuthority('AUDIT:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deleteAuditLogsBeforeDate(
      @Parameter(description = "Date threshold") @RequestParam LocalDateTime date) {
    auditService.deleteAuditLogsBeforeDate(date);
    return ResponseEntity.ok(ApiResponse.success("Audit logs deleted successfully", null));
  }

  public record CreateAuditLogRequest(
      @Schema(description = "Entity type") String entityType,
      @Schema(description = "Entity ID") UUID entityId,
      @Schema(description = "Action") AuditActionDto action,
      @Schema(description = "Description") String description,
      @Schema(description = "Old value") String oldValue,
      @Schema(description = "New value") String newValue,
      @Schema(description = "Changed fields") String changedFields,
      @Schema(description = "IP address") String ipAddress,
      @Schema(description = "User agent") String userAgent,
      @Schema(description = "Request ID") String requestId,
      @Schema(description = "Session ID") String sessionId,
      @Schema(description = "User creating the audit log") String createdBy) {}
}
