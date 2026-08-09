package com.finance.smartLedger.notification.application;

import com.finance.smartLedger.notification.domain.Notification;
import com.finance.smartLedger.notification.domain.NotificationChannel;
import com.finance.smartLedger.notification.domain.NotificationStatus;
import com.finance.smartLedger.notification.domain.NotificationType;
import com.finance.smartLedger.notification.infrastructure.persistence.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final com.finance.smartLedger.audit.application.AuditService auditService;
  private final java.util.Optional<com.finance.smartLedger.notification.infrastructure.email.EmailService> emailService;

  @Transactional
  public Notification createNotification(
      String recipientEmail,
      String recipientPhone,
      NotificationType notificationType,
      NotificationChannel channel,
      String subject,
      String message,
      String relatedEntityType,
      UUID relatedEntityId,
      String createdBy) {
    Notification notification =
        new Notification(
            recipientEmail,
            recipientPhone,
            notificationType,
            channel,
            subject,
            message,
            relatedEntityType,
            relatedEntityId,
            createdBy);

    Notification savedNotification = notificationRepository.save(notification);

    // Audit log for notification creation
    auditService.logCreate(
        "Notification",
        savedNotification.getId(),
        "Notification created: " + notificationType.name(),
        savedNotification.toString(),
        createdBy);

    return savedNotification;
  }

  @Transactional
  public Notification sendNotification(UUID notificationId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

    String oldStatus = notification.getStatus().name();

    // Send email if channel is EMAIL and recipient email is present
    if (notification.getChannel() == NotificationChannel.EMAIL
        && notification.getRecipientEmail() != null) {
      try {
        emailService.ifPresent(es -> es.sendEmail(
            notification.getRecipientEmail(), notification.getSubject(), notification.getMessage()));
      } catch (Exception e) {
        // Mark as failed if email sending fails
        notification.markAsFailed(e.getMessage());
        Notification savedNotification = notificationRepository.save(notification);
        auditService.logStatusChange(
            "Notification",
            savedNotification.getId(),
            "Notification status changed to FAILED",
            oldStatus,
            savedNotification.getStatus().name(),
            "system");
        return savedNotification;
      }
    }

    notification.markAsSent();
    Notification savedNotification = notificationRepository.save(notification);

    // Audit log for notification status change
    auditService.logStatusChange(
        "Notification",
        savedNotification.getId(),
        "Notification status changed to SENT",
        oldStatus,
        savedNotification.getStatus().name(),
        "system");

    return savedNotification;
  }

  @Transactional
  public Notification markAsDelivered(UUID notificationId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

    String oldStatus = notification.getStatus().name();
    notification.markAsDelivered();
    Notification savedNotification = notificationRepository.save(notification);

    // Audit log for notification status change
    auditService.logStatusChange(
        "Notification",
        savedNotification.getId(),
        "Notification status changed to DELIVERED",
        oldStatus,
        savedNotification.getStatus().name(),
        "system");

    return savedNotification;
  }

  @Transactional
  public Notification markAsFailed(UUID notificationId, String errorMessage) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

    String oldStatus = notification.getStatus().name();
    notification.markAsFailed(errorMessage);
    Notification savedNotification = notificationRepository.save(notification);

    // Audit log for notification status change
    auditService.logStatusChange(
        "Notification",
        savedNotification.getId(),
        "Notification status changed to FAILED",
        oldStatus,
        savedNotification.getStatus().name(),
        "system");

    return savedNotification;
  }

  @Transactional
  public Notification retryNotification(UUID notificationId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

    if (!notification.canRetry()) {
      throw new IllegalStateException("Notification cannot be retried");
    }

    notification.markForRetry();
    return notificationRepository.save(notification);
  }

  @Transactional
  public List<Notification> processRetryableNotifications() {
    List<Notification> retryableNotifications =
        notificationRepository.findRetryableNotifications(NotificationStatus.FAILED);

    retryableNotifications.forEach(
        notification -> {
          notification.markForRetry();
          notificationRepository.save(notification);
        });

    return retryableNotifications;
  }

  @Transactional
  public List<Notification> processScheduledNotifications() {
    List<Notification> scheduledNotifications =
        notificationRepository.findScheduledNotifications(
            LocalDateTime.now(), NotificationStatus.PENDING);

    scheduledNotifications.forEach(
        notification -> {
          notification.markAsSent();
          notificationRepository.save(notification);
        });

    return scheduledNotifications;
  }

  public Optional<Notification> findById(UUID id) {
    return notificationRepository.findById(id);
  }

  public List<Notification> findByRecipientEmail(String recipientEmail) {
    return notificationRepository.findByRecipientEmail(recipientEmail);
  }

  public List<Notification> findByRecipientPhone(String recipientPhone) {
    return notificationRepository.findByRecipientPhone(recipientPhone);
  }

  public List<Notification> findByNotificationType(NotificationType notificationType) {
    return notificationRepository.findByNotificationType(notificationType);
  }

  public List<Notification> findByChannel(NotificationChannel channel) {
    return notificationRepository.findByChannel(channel);
  }

  public List<Notification> findByStatus(NotificationStatus status) {
    return notificationRepository.findByStatus(status);
  }

  public List<Notification> findByRelatedEntity(String relatedEntityType, UUID relatedEntityId) {
    return notificationRepository.findByRelatedEntity(relatedEntityType, relatedEntityId);
  }

  public List<Notification> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return notificationRepository.findByCreatedAtBetween(startDate, endDate);
  }

  @Transactional
  public void deleteNotification(UUID id) {
    notificationRepository.deleteById(id);
  }

  // Convenience methods for common notification types
  @Transactional
  public Notification sendPaymentCompletedNotification(
      String recipientEmail,
      String recipientPhone,
      String paymentNumber,
      String amount,
      String currencyCode,
      UUID paymentId,
      String createdBy) {
    String subject = "Payment Completed - " + paymentNumber;
    String message =
        String.format(
            "Your payment of %s %s has been successfully completed. Payment Number: %s",
            amount, currencyCode, paymentNumber);

    return createNotification(
        recipientEmail,
        recipientPhone,
        NotificationType.PAYMENT_COMPLETED,
        NotificationChannel.EMAIL,
        subject,
        message,
        "Payment",
        paymentId,
        createdBy);
  }

  @Transactional
  public Notification sendPaymentFailedNotification(
      String recipientEmail,
      String recipientPhone,
      String paymentNumber,
      String errorMessage,
      UUID paymentId,
      String createdBy) {
    String subject = "Payment Failed - " + paymentNumber;
    String message =
        String.format(
            "Your payment %s has failed. Reason: %s. Please try again or contact support.",
            paymentNumber, errorMessage);

    return createNotification(
        recipientEmail,
        recipientPhone,
        NotificationType.PAYMENT_FAILED,
        NotificationChannel.EMAIL,
        subject,
        message,
        "Payment",
        paymentId,
        createdBy);
  }

  @Transactional
  public Notification sendReceiptGeneratedNotification(
      String recipientEmail,
      String recipientPhone,
      String receiptNumber,
      String paymentNumber,
      UUID receiptId,
      String createdBy) {
    String subject = "Receipt Generated - " + receiptNumber;
    String message =
        String.format(
            "Your receipt %s for payment %s has been generated and will be sent to you shortly.",
            receiptNumber, paymentNumber);

    return createNotification(
        recipientEmail,
        recipientPhone,
        NotificationType.RECEIPT_GENERATED,
        NotificationChannel.EMAIL,
        subject,
        message,
        "Receipt",
        receiptId,
        createdBy);
  }

  @Transactional
  public Notification sendReceiptDeliveredNotification(
      String recipientEmail,
      String recipientPhone,
      String receiptNumber,
      UUID receiptId,
      String createdBy) {
    String subject = "Receipt Delivered - " + receiptNumber;
    String message =
        "Your receipt " + receiptNumber + " has been successfully delivered to your email.";

    return createNotification(
        recipientEmail,
        recipientPhone,
        NotificationType.RECEIPT_DELIVERED,
        NotificationChannel.EMAIL,
        subject,
        message,
        "Receipt",
        receiptId,
        createdBy);
  }
}
