package com.finance.smartLedger.notification.infrastructure.persistence;

import com.finance.smartLedger.notification.domain.Notification;
import com.finance.smartLedger.notification.domain.NotificationChannel;
import com.finance.smartLedger.notification.domain.NotificationStatus;
import com.finance.smartLedger.notification.domain.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository
    extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

  @Query("SELECT n FROM Notification n WHERE n.recipientEmail = :recipientEmail")
  List<Notification> findByRecipientEmail(@Param("recipientEmail") String recipientEmail);

  @Query("SELECT n FROM Notification n WHERE n.recipientPhone = :recipientPhone")
  List<Notification> findByRecipientPhone(@Param("recipientPhone") String recipientPhone);

  @Query("SELECT n FROM Notification n WHERE n.notificationType = :notificationType")
  List<Notification> findByNotificationType(
      @Param("notificationType") NotificationType notificationType);

  @Query("SELECT n FROM Notification n WHERE n.channel = :channel")
  List<Notification> findByChannel(@Param("channel") NotificationChannel channel);

  @Query("SELECT n FROM Notification n WHERE n.status = :status")
  List<Notification> findByStatus(@Param("status") NotificationStatus status);

  @Query(
      "SELECT n FROM Notification n WHERE n.relatedEntityType = :relatedEntityType AND n.relatedEntityId = :relatedEntityId")
  List<Notification> findByRelatedEntity(
      @Param("relatedEntityType") String relatedEntityType,
      @Param("relatedEntityId") UUID relatedEntityId);

  @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < n.maxRetries")
  List<Notification> findRetryableNotifications(@Param("status") NotificationStatus status);

  @Query(
      "SELECT n FROM Notification n WHERE n.scheduledAt IS NOT NULL AND n.scheduledAt <= :scheduledAt AND n.status = :status")
  List<Notification> findScheduledNotifications(
      @Param("scheduledAt") LocalDateTime scheduledAt, @Param("status") NotificationStatus status);

  @Query("SELECT n FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate")
  List<Notification> findByCreatedAtBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT n FROM Notification n WHERE n.status = :status ORDER BY n.createdAt DESC")
  List<Notification> findByStatusOrderByCreatedAtDesc(@Param("status") NotificationStatus status);
}
