package com.finance.smartLedger.notification.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Notification extends AuditableEntity {

  @Column(name = "recipient_email", length = 100)
  private String recipientEmail;

  @Column(name = "recipient_phone", length = 20)
  private String recipientPhone;

  @Enumerated(EnumType.STRING)
  @Column(name = "notification_type", nullable = false, length = 50)
  private NotificationType notificationType;

  @Enumerated(EnumType.STRING)
  @Column(name = "channel", nullable = false, length = 20)
  private NotificationChannel channel;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private NotificationStatus status;

  @Column(name = "subject", length = 200)
  private String subject;

  @Column(name = "message", nullable = false, columnDefinition = "TEXT")
  private String message;

  @Column(name = "related_entity_type", length = 50)
  private String relatedEntityType;

  @Column(name = "related_entity_id")
  private UUID relatedEntityId;

  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  @Column(name = "delivered_at")
  private LocalDateTime deliveredAt;

  @Column(name = "failed_at")
  private LocalDateTime failedAt;

  @Column(name = "error_message", length = 500)
  private String errorMessage;

  @Column(name = "retry_count")
  private Integer retryCount;

  @Column(name = "max_retries")
  private Integer maxRetries;

  @Column(name = "last_retry_at")
  private LocalDateTime lastRetryAt;

  @Column(name = "scheduled_at")
  private LocalDateTime scheduledAt;

  @Column(name = "metadata")
  private String metadata;

  public Notification(
      String recipientEmail,
      String recipientPhone,
      NotificationType notificationType,
      NotificationChannel channel,
      String subject,
      String message,
      String relatedEntityType,
      UUID relatedEntityId,
      String createdBy) {
    this.recipientEmail = recipientEmail;
    this.recipientPhone = recipientPhone;
    this.notificationType = notificationType;
    this.channel = channel;
    this.status = NotificationStatus.PENDING;
    this.subject = subject;
    this.message = message;
    this.relatedEntityType = relatedEntityType;
    this.relatedEntityId = relatedEntityId;
    this.retryCount = 0;
    this.maxRetries = 3;
    this.setCreatedBy(createdBy);
    this.setUpdatedBy(createdBy);
  }

  public void markAsSent() {
    if (status != NotificationStatus.PENDING && status != NotificationStatus.RETRYING) {
      throw new IllegalStateException("Can only mark pending or retrying notifications as sent");
    }
    this.status = NotificationStatus.SENT;
    this.sentAt = LocalDateTime.now();
  }

  public void markAsDelivered() {
    if (status != NotificationStatus.SENT) {
      throw new IllegalStateException("Can only mark sent notifications as delivered");
    }
    this.status = NotificationStatus.DELIVERED;
    this.deliveredAt = LocalDateTime.now();
  }

  public void markAsFailed(String errorMessage) {
    this.status = NotificationStatus.FAILED;
    this.errorMessage = errorMessage;
    this.failedAt = LocalDateTime.now();
  }

  public void markForRetry() {
    if (retryCount >= maxRetries) {
      throw new IllegalStateException("Max retries exceeded");
    }
    this.status = NotificationStatus.RETRYING;
    this.retryCount++;
    this.lastRetryAt = LocalDateTime.now();
  }

  public boolean canRetry() {
    return retryCount < maxRetries;
  }
}
