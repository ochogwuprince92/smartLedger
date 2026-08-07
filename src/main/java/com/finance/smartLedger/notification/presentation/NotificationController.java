package com.finance.smartLedger.notification.presentation;

import com.finance.smartLedger.notification.application.NotificationService;
import com.finance.smartLedger.notification.application.dto.NotificationChannelDto;
import com.finance.smartLedger.notification.application.dto.NotificationResponse;
import com.finance.smartLedger.notification.application.dto.NotificationTypeDto;
import com.finance.smartLedger.notification.domain.Notification;
import com.finance.smartLedger.notification.domain.NotificationChannel;
import com.finance.smartLedger.notification.domain.NotificationStatus;
import com.finance.smartLedger.notification.domain.NotificationType;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Notification management endpoints")
public class NotificationController {

  private final NotificationService notificationService;

  @PostMapping("/notifications")
  @Operation(summary = "Create notification", description = "Creates a new notification")
  @PreAuthorize("hasAuthority('NOTIFICATION:CREATE')")
  public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
      @RequestBody @Valid CreateNotificationRequest request) {
    Notification notification =
        notificationService.createNotification(
            request.recipientEmail(),
            request.recipientPhone(),
            request.notificationType().toDomain(),
            request.channel().toDomain(),
            request.subject(),
            request.message(),
            request.relatedEntityType(),
            request.relatedEntityId(),
            request.createdBy());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Notification created successfully", NotificationResponse.from(notification)));
  }

  @PostMapping("/notifications/{id}/send")
  @Operation(summary = "Send notification", description = "Marks a notification as sent")
  @PreAuthorize("hasAuthority('NOTIFICATION:UPDATE')")
  public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
      @Parameter(description = "Notification ID") @PathVariable UUID id,
      @RequestBody @Schema(description = "User sending the notification") ActionRequest request) {
    Notification notification = notificationService.sendNotification(id);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Notification sent successfully", NotificationResponse.from(notification)));
  }

  @PostMapping("/notifications/{id}/deliver")
  @Operation(
      summary = "Mark notification as delivered",
      description = "Marks a notification as delivered")
  @PreAuthorize("hasAuthority('NOTIFICATION:UPDATE')")
  public ResponseEntity<ApiResponse<NotificationResponse>> markAsDelivered(
      @Parameter(description = "Notification ID") @PathVariable UUID id,
      @RequestBody @Schema(description = "User marking the notification as delivered")
          ActionRequest request) {
    Notification notification = notificationService.markAsDelivered(id);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Notification marked as delivered", NotificationResponse.from(notification)));
  }

  @PostMapping("/notifications/{id}/fail")
  @Operation(
      summary = "Mark notification as failed",
      description = "Marks a notification as failed")
  @PreAuthorize("hasAuthority('NOTIFICATION:UPDATE')")
  public ResponseEntity<ApiResponse<NotificationResponse>> markAsFailed(
      @Parameter(description = "Notification ID") @PathVariable UUID id,
      @RequestBody FailNotificationRequest request) {
    Notification notification = notificationService.markAsFailed(id, request.errorMessage());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Notification marked as failed", NotificationResponse.from(notification)));
  }

  @PostMapping("/notifications/{id}/retry")
  @Operation(summary = "Retry notification", description = "Retries a failed notification")
  @PreAuthorize("hasAuthority('NOTIFICATION:UPDATE')")
  public ResponseEntity<ApiResponse<NotificationResponse>> retryNotification(
      @Parameter(description = "Notification ID") @PathVariable UUID id,
      @RequestBody @Schema(description = "User retrying the notification") ActionRequest request) {
    Notification notification = notificationService.retryNotification(id);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Notification retry initiated", NotificationResponse.from(notification)));
  }

  @PostMapping("/notifications/process-retryable")
  @Operation(
      summary = "Process retryable notifications",
      description = "Processes all failed notifications that can be retried")
  @PreAuthorize("hasAuthority('NOTIFICATION:UPDATE')")
  public ResponseEntity<ApiResponse<List<NotificationResponse>>> processRetryableNotifications() {
    List<Notification> notifications = notificationService.processRetryableNotifications();
    List<NotificationResponse> responses =
        notifications.stream().map(NotificationResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success("Retryable notifications processed", responses));
  }

  @PostMapping("/notifications/process-scheduled")
  @Operation(
      summary = "Process scheduled notifications",
      description = "Processes all scheduled notifications")
  @PreAuthorize("hasAuthority('NOTIFICATION:UPDATE')")
  public ResponseEntity<ApiResponse<List<NotificationResponse>>> processScheduledNotifications() {
    List<Notification> notifications = notificationService.processScheduledNotifications();
    List<NotificationResponse> responses =
        notifications.stream().map(NotificationResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success("Scheduled notifications processed", responses));
  }

  @GetMapping("/notifications/{id}")
  @Operation(summary = "Get notification by ID", description = "Retrieves a notification by its ID")
  @PreAuthorize("hasAuthority('NOTIFICATION:READ')")
  public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(
      @Parameter(description = "Notification ID") @PathVariable UUID id) {
    Notification notification =
        notificationService
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
    return ResponseEntity.ok(
        ApiResponse.success(
            "Notification retrieved successfully", NotificationResponse.from(notification)));
  }

  @GetMapping("/notifications")
  @Operation(
      summary = "List notifications",
      description = "Lists all notifications with optional filters")
  @PreAuthorize("hasAuthority('NOTIFICATION:READ')")
  public ResponseEntity<ApiResponse<List<NotificationResponse>>> listNotifications(
      @Parameter(description = "Filter by recipient email") @RequestParam(required = false)
          String recipientEmail,
      @Parameter(description = "Filter by recipient phone") @RequestParam(required = false)
          String recipientPhone,
      @Parameter(description = "Filter by notification type") @RequestParam(required = false)
          NotificationType notificationType,
      @Parameter(description = "Filter by channel") @RequestParam(required = false)
          NotificationChannel channel,
      @Parameter(description = "Filter by status") @RequestParam(required = false)
          NotificationStatus status,
      @Parameter(description = "Filter by related entity type") @RequestParam(required = false)
          String relatedEntityType,
      @Parameter(description = "Filter by related entity ID") @RequestParam(required = false)
          UUID relatedEntityId,
      @Parameter(description = "Filter by start date") @RequestParam(required = false)
          LocalDateTime startDate,
      @Parameter(description = "Filter by end date") @RequestParam(required = false)
          LocalDateTime endDate) {
    List<Notification> notifications;

    if (recipientEmail != null) {
      notifications = notificationService.findByRecipientEmail(recipientEmail);
    } else if (recipientPhone != null) {
      notifications = notificationService.findByRecipientPhone(recipientPhone);
    } else if (notificationType != null) {
      notifications = notificationService.findByNotificationType(notificationType);
    } else if (channel != null) {
      notifications = notificationService.findByChannel(channel);
    } else if (status != null) {
      notifications = notificationService.findByStatus(status);
    } else if (relatedEntityType != null && relatedEntityId != null) {
      notifications = notificationService.findByRelatedEntity(relatedEntityType, relatedEntityId);
    } else if (startDate != null && endDate != null) {
      notifications = notificationService.findByCreatedAtBetween(startDate, endDate);
    } else {
      notifications = notificationService.findByStatus(NotificationStatus.PENDING);
    }

    List<NotificationResponse> responses =
        notifications.stream().map(NotificationResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @DeleteMapping("/notifications/{id}")
  @Operation(summary = "Delete notification", description = "Deletes a notification")
  @PreAuthorize("hasAuthority('NOTIFICATION:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deleteNotification(
      @Parameter(description = "Notification ID") @PathVariable UUID id) {
    notificationService.deleteNotification(id);
    return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
  }

  public record ActionRequest(
      @Schema(description = "User performing the action") String updatedBy) {}

  public record CreateNotificationRequest(
      @Schema(description = "Recipient email") String recipientEmail,
      @Schema(description = "Recipient phone") String recipientPhone,
      @Schema(description = "Notification type") NotificationTypeDto notificationType,
      @Schema(description = "Channel") NotificationChannelDto channel,
      @Schema(description = "Subject") String subject,
      @Schema(description = "Message") String message,
      @Schema(description = "Related entity type") String relatedEntityType,
      @Schema(description = "Related entity ID") UUID relatedEntityId,
      @Schema(description = "User creating the notification") String createdBy) {}

  public record FailNotificationRequest(
      @Schema(description = "Error message") String errorMessage,
      @Schema(description = "User marking the notification as failed") String updatedBy) {}
}
