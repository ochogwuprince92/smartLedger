package com.finance.smartLedger.notification.application.dto;

import com.finance.smartLedger.notification.domain.Notification;
import com.finance.smartLedger.notification.domain.NotificationChannel;
import com.finance.smartLedger.notification.domain.NotificationStatus;
import com.finance.smartLedger.notification.domain.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response for a notification")
public record NotificationResponse(
    @Schema(description = "Notification ID") UUID id,
    @Schema(description = "Recipient email") String recipientEmail,
    @Schema(description = "Recipient phone") String recipientPhone,
    @Schema(description = "Notification type") NotificationType notificationType,
    @Schema(description = "Channel") NotificationChannel channel,
    @Schema(description = "Status") NotificationStatus status,
    @Schema(description = "Subject") String subject,
    @Schema(description = "Message") String message,
    @Schema(description = "Related entity type") String relatedEntityType,
    @Schema(description = "Related entity ID") UUID relatedEntityId,
    @Schema(description = "Sent at") LocalDateTime sentAt,
    @Schema(description = "Delivered at") LocalDateTime deliveredAt,
    @Schema(description = "Failed at") LocalDateTime failedAt,
    @Schema(description = "Error message") String errorMessage,
    @Schema(description = "Retry count") Integer retryCount,
    @Schema(description = "Max retries") Integer maxRetries,
    @Schema(description = "Scheduled at") LocalDateTime scheduledAt,
    @Schema(description = "Metadata") String metadata,
    @Schema(description = "Created at") LocalDateTime createdAt,
    @Schema(description = "Updated at") LocalDateTime updatedAt) {

  public static NotificationResponse from(Notification notification) {
    return new NotificationResponse(
        notification.getId(),
        notification.getRecipientEmail(),
        notification.getRecipientPhone(),
        notification.getNotificationType(),
        notification.getChannel(),
        notification.getStatus(),
        notification.getSubject(),
        notification.getMessage(),
        notification.getRelatedEntityType(),
        notification.getRelatedEntityId(),
        notification.getSentAt(),
        notification.getDeliveredAt(),
        notification.getFailedAt(),
        notification.getErrorMessage(),
        notification.getRetryCount(),
        notification.getMaxRetries(),
        notification.getScheduledAt(),
        notification.getMetadata(),
        notification.getCreatedAt(),
        notification.getUpdatedAt());
  }
}
